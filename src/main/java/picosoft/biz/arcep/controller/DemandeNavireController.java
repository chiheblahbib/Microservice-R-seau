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
import picosoft.biz.arcep.domain.navire.DemandeNavire;
import picosoft.biz.arcep.service.DemandeNavireQueryService;
import picosoft.biz.arcep.service.DemandeNavireService;
import picosoft.biz.arcep.service.criteria.DemandeNavireCriteria;
import picosoft.biz.arcep.service.dto.DemandeNavireDTO;
import picosoft.biz.arcep.service.dto.DemandeNavireInputDTO;
import picosoft.biz.arcep.service.dto.DemandeNavireOutputDTO;

import java.util.List;

/**
 * Dossier de reseau. Mince par construction : il resout l'AclClass
 * et delegue au service, comme les controleurs d'homologation.
 */
@RestController
@RequestMapping("/api")
public class DemandeNavireController {

    private final DemandeNavireService demandeNavireService;
    private final DemandeNavireQueryService demandeNavireQueryService;
    private final KernelInterface kernelInterface;
    private final KernelService kernelService;
    private final CurrentUser currentUser;

    public DemandeNavireController(DemandeNavireService demandeNavireService,
                                         DemandeNavireQueryService demandeNavireQueryService,
                                         KernelInterface kernelInterface,
                                         KernelService kernelService,
                                         CurrentUser currentUser) {
        this.demandeNavireService = demandeNavireService;
        this.demandeNavireQueryService = demandeNavireQueryService;
        this.kernelInterface = kernelInterface;
        this.kernelService = kernelService;
        this.currentUser = currentUser;
    }

    private AclClass aclClass() {
        return kernelInterface.getaclClassByClassName(DemandeNavire.class.getName());
    }

    // ------------------------------------------------------------------ CRUD

    @PostMapping("/demande-navires")
    public ResponseEntity<DemandeNavireDTO> create(@RequestBody @Valid DemandeNavireDTO dto) {
        return ResponseEntity.ok(demandeNavireService.save(dto));
    }

    @PutMapping("/demande-navires/{id}")
    public ResponseEntity<DemandeNavireDTO> update(@PathVariable Long id,
                                                         @RequestBody @Valid DemandeNavireDTO dto) {
        return ResponseEntity.ok(demandeNavireService.update(id, dto));
    }

    @GetMapping("/demande-navires")
    public ResponseEntity<Page<DemandeNavireDTO>> getAll(DemandeNavireCriteria criteria,
                                                               Pageable pageable,
                                                               @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(demandeNavireService.findAll(criteria, pageable, size));
    }

    /**
     * Meme requete, restreinte aux dossiers que l'utilisateur courant a le droit de voir.
     */
    @GetMapping("/demande-navires/mine")
    public ResponseEntity<Page<DemandeNavireDTO>> getMine(DemandeNavireCriteria criteria,
                                                                Pageable pageable,
                                                                @RequestParam(value = "size", required = false) Integer size,
                                                                @RequestParam(value = "masks", required = false) List<Integer> masks) {
        List<Integer> masquesEffectifs = (masks == null || masks.isEmpty()) ? List.of(1, 2, 4) : masks;
        return ResponseEntity.ok(demandeNavireQueryService.findByCriteriaAcl(
                criteria, pageable, size, currentUser.getSid(), masquesEffectifs));
    }

    @GetMapping("/demande-navires/{id}")
    public ResponseEntity<DemandeNavireDTO> getOne(@PathVariable Long id) {
        return demandeNavireService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority(@kernelService.navire_role_canReadNavire())")
    @GetMapping("/demande-navire/{id}")
    public DemandeNavireOutputDTO detail(@PathVariable Long id) {
        return demandeNavireService.byId(id);
    }

    @DeleteMapping("/demande-navires/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        demandeNavireService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- workflow

    @PreAuthorize("hasAuthority(@kernelService.navire_role_canCreateNavire())")
    @PatchMapping("/firstSubmitDemandeNavire")
    public DemandeNavireOutputDTO firstSubmit(@RequestBody @Valid DemandeNavireInputDTO input) throws Exception {
        // Valide AVANT aclClass() : celui-ci est un appel Feign au kernel. Sans cet
        // ordre, un dossier incomplet exigerait un aller-retour reseau pour se voir
        // refuser, et une panne du kernel masquerait la vraie raison du refus.
        demandeNavireService.exigerPourSoumission(input);
        return demandeNavireService.initAndSubmit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.navire_role_canEditNavire())")
    @PatchMapping("/submitDemandeNavire")
    public DemandeNavireOutputDTO submit(@RequestBody @Valid DemandeNavireInputDTO input) throws Exception {
        demandeNavireService.exigerPourSoumission(input);
        return demandeNavireService.submit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.navire_role_canEditNavire())")
    @PatchMapping("/saveDemandeNavireAsDraft")
    public DemandeNavireOutputDTO saveAsDraft(@RequestBody @Valid DemandeNavireInputDTO input) {
        return demandeNavireService.saveAsDraft(input, aclClass());
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

    @GetMapping("/initClassDemandeNavire")
    public void initClassDemandeNavire() {
        kernelService.initVaraible();
        kernelService.initSequences();
        kernelService.initClassDemandeNavire();
    }
}
