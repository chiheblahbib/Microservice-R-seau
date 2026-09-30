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
import picosoft.biz.arcep.domain.installateur.DemandeInstallateur;
import picosoft.biz.arcep.service.DemandeInstallateurQueryService;
import picosoft.biz.arcep.service.DemandeInstallateurService;
import picosoft.biz.arcep.service.criteria.DemandeInstallateurCriteria;
import picosoft.biz.arcep.service.dto.DemandeInstallateurDTO;
import picosoft.biz.arcep.service.dto.DemandeInstallateurInputDTO;
import picosoft.biz.arcep.service.dto.DemandeInstallateurOutputDTO;

import java.util.List;

/**
 * Dossier de reseau. Mince par construction : il resout l'AclClass
 * et delegue au service, comme les controleurs d'homologation.
 */
@RestController
@RequestMapping("/api")
public class DemandeInstallateurController {

    private final DemandeInstallateurService demandeInstallateurService;
    private final DemandeInstallateurQueryService demandeInstallateurQueryService;
    private final KernelInterface kernelInterface;
    private final KernelService kernelService;
    private final CurrentUser currentUser;

    public DemandeInstallateurController(DemandeInstallateurService demandeInstallateurService,
                                         DemandeInstallateurQueryService demandeInstallateurQueryService,
                                         KernelInterface kernelInterface,
                                         KernelService kernelService,
                                         CurrentUser currentUser) {
        this.demandeInstallateurService = demandeInstallateurService;
        this.demandeInstallateurQueryService = demandeInstallateurQueryService;
        this.kernelInterface = kernelInterface;
        this.kernelService = kernelService;
        this.currentUser = currentUser;
    }

    private AclClass aclClass() {
        return kernelInterface.getaclClassByClassName(DemandeInstallateur.class.getName());
    }

    // ------------------------------------------------------------------ CRUD

    @PostMapping("/demande-installateurs")
    public ResponseEntity<DemandeInstallateurDTO> create(@RequestBody @Valid DemandeInstallateurDTO dto) {
        return ResponseEntity.ok(demandeInstallateurService.save(dto));
    }

    @PutMapping("/demande-installateurs/{id}")
    public ResponseEntity<DemandeInstallateurDTO> update(@PathVariable Long id,
                                                         @RequestBody @Valid DemandeInstallateurDTO dto) {
        return ResponseEntity.ok(demandeInstallateurService.update(id, dto));
    }

    @GetMapping("/demande-installateurs")
    public ResponseEntity<Page<DemandeInstallateurDTO>> getAll(DemandeInstallateurCriteria criteria,
                                                               Pageable pageable,
                                                               @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(demandeInstallateurService.findAll(criteria, pageable, size));
    }

    /**
     * Meme requete, restreinte aux dossiers que l'utilisateur courant a le droit de voir.
     */
    @GetMapping("/demande-installateurs/mine")
    public ResponseEntity<Page<DemandeInstallateurDTO>> getMine(DemandeInstallateurCriteria criteria,
                                                                Pageable pageable,
                                                                @RequestParam(value = "size", required = false) Integer size,
                                                                @RequestParam(value = "masks", required = false) List<Integer> masks) {
        List<Integer> masquesEffectifs = (masks == null || masks.isEmpty()) ? List.of(1, 2, 4) : masks;
        return ResponseEntity.ok(demandeInstallateurQueryService.findByCriteriaAcl(
                criteria, pageable, size, currentUser.getSid(), masquesEffectifs));
    }

    @GetMapping("/demande-installateurs/{id}")
    public ResponseEntity<DemandeInstallateurDTO> getOne(@PathVariable Long id) {
        return demandeInstallateurService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority(@kernelService.installateur_role_canReadInstallateur())")
    @GetMapping("/demande-installateur/{id}")
    public DemandeInstallateurOutputDTO detail(@PathVariable Long id) {
        return demandeInstallateurService.byId(id);
    }

    @PreAuthorize("hasAuthority(@kernelService.installateur_role_canEditInstallateur())")
    @DeleteMapping("/demande-installateurs/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        demandeInstallateurService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- workflow

    @PreAuthorize("hasAuthority(@kernelService.installateur_role_canCreateInstallateur())")
    @PatchMapping("/firstSubmitDemandeInstallateur")
    public DemandeInstallateurOutputDTO firstSubmit(@RequestBody @Valid DemandeInstallateurInputDTO input) throws Exception {
        // Valide AVANT aclClass() : celui-ci est un appel Feign au kernel. Sans cet
        // ordre, un dossier incomplet exigerait un aller-retour reseau pour se voir
        // refuser, et une panne du kernel masquerait la vraie raison du refus.
        demandeInstallateurService.exigerPourSoumission(input);
        return demandeInstallateurService.initAndSubmit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.installateur_role_canEditInstallateur())")
    @PatchMapping("/submitDemandeInstallateur")
    public DemandeInstallateurOutputDTO submit(@RequestBody @Valid DemandeInstallateurInputDTO input) throws Exception {
        demandeInstallateurService.exigerPourSoumission(input);
        return demandeInstallateurService.submit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.installateur_role_canEditInstallateur())")
    @PatchMapping("/saveDemandeInstallateurAsDraft")
    public DemandeInstallateurOutputDTO saveAsDraft(@RequestBody @Valid DemandeInstallateurInputDTO input) {
        return demandeInstallateurService.saveAsDraft(input, aclClass());
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

    @GetMapping("/initClassDemandeInstallateur")
    public void initClassDemandeInstallateur() {
        kernelService.initVaraible();
        kernelService.initSequences();
        kernelService.initClassDemandeInstallateur();
    }
}
