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
import picosoft.biz.arcep.domain.declaratif.DemandeDeclaratif;
import picosoft.biz.arcep.service.DemandeDeclaratifQueryService;
import picosoft.biz.arcep.service.DemandeDeclaratifService;
import picosoft.biz.arcep.service.criteria.DemandeDeclaratifCriteria;
import picosoft.biz.arcep.service.dto.DemandeDeclaratifDTO;
import picosoft.biz.arcep.service.dto.DemandeDeclaratifInputDTO;
import picosoft.biz.arcep.service.dto.DemandeDeclaratifOutputDTO;

import java.util.List;

/**
 * Dossier de reseau. Mince par construction : il resout l'AclClass
 * et delegue au service, comme les controleurs d'homologation.
 */
@RestController
@RequestMapping("/api")
public class DemandeDeclaratifController {

    private final DemandeDeclaratifService demandeDeclaratifService;
    private final DemandeDeclaratifQueryService demandeDeclaratifQueryService;
    private final KernelInterface kernelInterface;
    private final KernelService kernelService;
    private final CurrentUser currentUser;

    public DemandeDeclaratifController(DemandeDeclaratifService demandeDeclaratifService,
                                         DemandeDeclaratifQueryService demandeDeclaratifQueryService,
                                         KernelInterface kernelInterface,
                                         KernelService kernelService,
                                         CurrentUser currentUser) {
        this.demandeDeclaratifService = demandeDeclaratifService;
        this.demandeDeclaratifQueryService = demandeDeclaratifQueryService;
        this.kernelInterface = kernelInterface;
        this.kernelService = kernelService;
        this.currentUser = currentUser;
    }

    private AclClass aclClass() {
        return kernelInterface.getaclClassByClassName(DemandeDeclaratif.class.getName());
    }

    // ------------------------------------------------------------------ CRUD

    @PostMapping("/demande-declaratifs")
    public ResponseEntity<DemandeDeclaratifDTO> create(@RequestBody @Valid DemandeDeclaratifDTO dto) {
        return ResponseEntity.ok(demandeDeclaratifService.save(dto));
    }

    @PutMapping("/demande-declaratifs/{id}")
    public ResponseEntity<DemandeDeclaratifDTO> update(@PathVariable Long id,
                                                         @RequestBody @Valid DemandeDeclaratifDTO dto) {
        return ResponseEntity.ok(demandeDeclaratifService.update(id, dto));
    }

    @GetMapping("/demande-declaratifs")
    public ResponseEntity<Page<DemandeDeclaratifDTO>> getAll(DemandeDeclaratifCriteria criteria,
                                                               Pageable pageable,
                                                               @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(demandeDeclaratifService.findAll(criteria, pageable, size));
    }

    /**
     * Meme requete, restreinte aux dossiers que l'utilisateur courant a le droit de voir.
     */
    @GetMapping("/demande-declaratifs/mine")
    public ResponseEntity<Page<DemandeDeclaratifDTO>> getMine(DemandeDeclaratifCriteria criteria,
                                                                Pageable pageable,
                                                                @RequestParam(value = "size", required = false) Integer size,
                                                                @RequestParam(value = "masks", required = false) List<Integer> masks) {
        List<Integer> masquesEffectifs = (masks == null || masks.isEmpty()) ? List.of(1, 2, 4) : masks;
        return ResponseEntity.ok(demandeDeclaratifQueryService.findByCriteriaAcl(
                criteria, pageable, size, currentUser.getSid(), masquesEffectifs));
    }

    @GetMapping("/demande-declaratifs/{id}")
    public ResponseEntity<DemandeDeclaratifDTO> getOne(@PathVariable Long id) {
        return demandeDeclaratifService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority(@kernelService.declaratif_role_canReadDeclaratif())")
    @GetMapping("/demande-declaratif/{id}")
    public DemandeDeclaratifOutputDTO detail(@PathVariable Long id) {
        return demandeDeclaratifService.byId(id);
    }

    @DeleteMapping("/demande-declaratifs/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        demandeDeclaratifService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- workflow

    @PreAuthorize("hasAuthority(@kernelService.declaratif_role_canCreateDeclaratif())")
    @PatchMapping("/firstSubmitDemandeDeclaratif")
    public DemandeDeclaratifOutputDTO firstSubmit(@RequestBody @Valid DemandeDeclaratifInputDTO input) throws Exception {
        // Valide AVANT aclClass() : celui-ci est un appel Feign au kernel. Sans cet
        // ordre, un dossier incomplet exigerait un aller-retour reseau pour se voir
        // refuser, et une panne du kernel masquerait la vraie raison du refus.
        demandeDeclaratifService.exigerPourSoumission(input);
        return demandeDeclaratifService.initAndSubmit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.declaratif_role_canEditDeclaratif())")
    @PatchMapping("/submitDemandeDeclaratif")
    public DemandeDeclaratifOutputDTO submit(@RequestBody @Valid DemandeDeclaratifInputDTO input) throws Exception {
        demandeDeclaratifService.exigerPourSoumission(input);
        return demandeDeclaratifService.submit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.declaratif_role_canEditDeclaratif())")
    @PatchMapping("/saveDemandeDeclaratifAsDraft")
    public DemandeDeclaratifOutputDTO saveAsDraft(@RequestBody @Valid DemandeDeclaratifInputDTO input) {
        return demandeDeclaratifService.saveAsDraft(input, aclClass());
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

    @GetMapping("/initClassDemandeDeclaratif")
    public void initClassDemandeDeclaratif() {
        kernelService.initVaraible();
        kernelService.initSequences();
        kernelService.initClassDemandeDeclaratif();
    }
}
