package picosoft.biz.arcep.controller;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import picosoft.biz.arcep.client.currentuser.model.CurrentUser;
import picosoft.biz.arcep.client.kernel.intercomm.KernelInterface;
import picosoft.biz.arcep.client.kernel.intercomm.KernelService;
import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.service.DemandeReseauQueryService;
import picosoft.biz.arcep.service.DemandeReseauService;
import picosoft.biz.arcep.service.criteria.DemandeReseauCriteria;
import picosoft.biz.arcep.service.dto.DemandeReseauDTO;
import picosoft.biz.arcep.service.dto.DemandeReseauInputDTO;
import picosoft.biz.arcep.service.dto.DemandeReseauOutputDTO;

import java.util.List;

/**
 * Dossier de reseau. Mince par construction : il resout l'AclClass
 * et delegue au service, comme les controleurs d'homologation.
 */
@RestController
@RequestMapping("/api")
public class DemandeReseauController {

    private final DemandeReseauService demandeReseauService;
    private final DemandeReseauQueryService demandeReseauQueryService;
    private final KernelInterface kernelInterface;
    private final KernelService kernelService;
    private final CurrentUser currentUser;

    public DemandeReseauController(DemandeReseauService demandeReseauService,
                                         DemandeReseauQueryService demandeReseauQueryService,
                                         KernelInterface kernelInterface,
                                         KernelService kernelService,
                                         CurrentUser currentUser) {
        this.demandeReseauService = demandeReseauService;
        this.demandeReseauQueryService = demandeReseauQueryService;
        this.kernelInterface = kernelInterface;
        this.kernelService = kernelService;
        this.currentUser = currentUser;
    }

    private AclClass aclClass() {
        return kernelInterface.getaclClassByClassName(DemandeReseau.class.getName());
    }

    // ------------------------------------------------------------------ CRUD

    @PostMapping("/demande-reseaux")
    public ResponseEntity<DemandeReseauDTO> create(@RequestBody @Valid DemandeReseauDTO dto) {
        return ResponseEntity.ok(demandeReseauService.save(dto));
    }

    @PutMapping("/demande-reseaux/{id}")
    public ResponseEntity<DemandeReseauDTO> update(@PathVariable Long id,
                                                         @RequestBody @Valid DemandeReseauDTO dto) {
        return ResponseEntity.ok(demandeReseauService.update(id, dto));
    }

    @GetMapping("/demande-reseaux")
    public ResponseEntity<Page<DemandeReseauDTO>> getAll(DemandeReseauCriteria criteria,
                                                               Pageable pageable,
                                                               @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(demandeReseauService.findAll(criteria, pageable, size));
    }

    /**
     * Meme requete, restreinte aux dossiers que l'utilisateur courant a le droit de voir.
     */
    @GetMapping("/demande-reseaux/mine")
    public ResponseEntity<Page<DemandeReseauDTO>> getMine(DemandeReseauCriteria criteria,
                                                                Pageable pageable,
                                                                @RequestParam(value = "size", required = false) Integer size,
                                                                @RequestParam(value = "masks", required = false) List<Integer> masks) {
        List<Integer> masquesEffectifs = (masks == null || masks.isEmpty()) ? List.of(1, 2, 4) : masks;
        return ResponseEntity.ok(demandeReseauQueryService.findByCriteriaAcl(
                criteria, pageable, size, currentUser.getSid(), masquesEffectifs));
    }

    @GetMapping("/demande-reseaux/{id}")
    public ResponseEntity<DemandeReseauDTO> getOne(@PathVariable Long id) {
        return demandeReseauService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority(@kernelService.reseau_role_canReadReseau())")
    @GetMapping("/demande-reseau/{id}")
    public DemandeReseauOutputDTO detail(@PathVariable Long id) {
        return demandeReseauService.byId(id);
    }

    @DeleteMapping("/demande-reseaux/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        demandeReseauService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- workflow

    @PreAuthorize("hasAuthority(@kernelService.reseau_role_canCreateReseau())")
    @PatchMapping("/firstSubmitDemandeReseau")
    public DemandeReseauOutputDTO firstSubmit(@RequestBody @Valid DemandeReseauInputDTO input) throws Exception {
        // Valide AVANT aclClass() : celui-ci est un appel Feign au kernel. Sans cet
        // ordre, un dossier incomplet exigerait un aller-retour reseau pour se voir
        // refuser, et une panne du kernel masquerait la vraie raison du refus.
        demandeReseauService.exigerPourSoumission(input);
        return demandeReseauService.initAndSubmit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.reseau_role_canEditReseau())")
    @PatchMapping("/submitDemandeReseau")
    public DemandeReseauOutputDTO submit(@RequestBody @Valid DemandeReseauInputDTO input) throws Exception {
        demandeReseauService.exigerPourSoumission(input);
        return demandeReseauService.submit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.reseau_role_canEditReseau())")
    @PatchMapping("/saveDemandeReseauAsDraft")
    public DemandeReseauOutputDTO saveAsDraft(@RequestBody @Valid DemandeReseauInputDTO input) {
        return demandeReseauService.saveAsDraft(input, aclClass());
    }

    // ------------------------------------------------------------------
    // Declaration du service aupres du kernel
    //
    // A INVOQUER UNE FOIS, A LA MAIN, apres un deploiement sur un nouvel
    // environnement. Ce n'est pas un demarrage automatique : rejouer ces
    // appels a chaque lancement reecrirait la configuration du kernel, et
    // masquerait une declaration faite entre-temps par un administrateur.
    //
    // AUCUN @PreAuthorize, volontairement, et c'est aussi ce que fait
    // homologation : les roles que ces methodes controleraient sont
    // precisement ceux que `initVaraible()` CREE. Les exiger ici rendrait la
    // declaration impossible -- on ne pourrait jamais franchir le premier
    // appel.
    //
    // CONSEQUENCE A TRAITER AVANT LA PRODUCTION : ces trois adresses sont
    // ouvertes. Elles doivent etre fermees au public par le reverse proxy,
    // au meme titre que celles d'homologation.
    // ------------------------------------------------------------------

    /**
     * Declare la classe ACL du DOSSIER, ses variables et ses sequences.
     *
     * L'ordre compte : les variables portent les roles, les sequences
     * alimentent la numerotation, et la classe s'appuie sur les deux.
     */
    @GetMapping("/initClassDemandeReseau")
    public void initClassDemandeReseau() {
        kernelService.initVaraible();
        kernelService.initSequences();
        kernelService.initClassDemandeReseau();
    }
}
