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
import picosoft.biz.arcep.domain.implantation.DemandeImplantation;
import picosoft.biz.arcep.service.DemandeImplantationQueryService;
import picosoft.biz.arcep.service.DemandeImplantationService;
import picosoft.biz.arcep.service.criteria.DemandeImplantationCriteria;
import picosoft.biz.arcep.service.dto.DemandeImplantationDTO;
import picosoft.biz.arcep.service.dto.DemandeImplantationInputDTO;
import picosoft.biz.arcep.service.dto.DemandeImplantationOutputDTO;

import java.util.List;

/**
 * Dossier parent d'implantation. Mince par construction : il resout l'AclClass
 * et delegue au service, comme les controleurs d'homologation.
 */
@RestController
@RequestMapping("/api")
public class DemandeImplantationController {

    private final DemandeImplantationService demandeImplantationService;
    private final DemandeImplantationQueryService demandeImplantationQueryService;
    private final KernelInterface kernelInterface;
    private final KernelService kernelService;
    private final CurrentUser currentUser;

    public DemandeImplantationController(DemandeImplantationService demandeImplantationService,
                                         DemandeImplantationQueryService demandeImplantationQueryService,
                                         KernelInterface kernelInterface,
                                         KernelService kernelService,
                                         CurrentUser currentUser) {
        this.demandeImplantationService = demandeImplantationService;
        this.demandeImplantationQueryService = demandeImplantationQueryService;
        this.kernelInterface = kernelInterface;
        this.kernelService = kernelService;
        this.currentUser = currentUser;
    }

    private AclClass aclClass() {
        return kernelInterface.getaclClassByClassName(DemandeImplantation.class.getName());
    }

    // ------------------------------------------------------------------ CRUD

    @PostMapping("/demande-implantations")
    public ResponseEntity<DemandeImplantationDTO> create(@RequestBody @Valid DemandeImplantationDTO dto) {
        return ResponseEntity.ok(demandeImplantationService.save(dto));
    }

    @PutMapping("/demande-implantations/{id}")
    public ResponseEntity<DemandeImplantationDTO> update(@PathVariable Long id,
                                                         @RequestBody @Valid DemandeImplantationDTO dto) {
        return ResponseEntity.ok(demandeImplantationService.update(id, dto));
    }

    @GetMapping("/demande-implantations")
    public ResponseEntity<Page<DemandeImplantationDTO>> getAll(DemandeImplantationCriteria criteria,
                                                               Pageable pageable,
                                                               @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(demandeImplantationService.findAll(criteria, pageable, size));
    }

    /**
     * Meme requete, restreinte aux dossiers que l'utilisateur courant a le droit de voir.
     */
    @GetMapping("/demande-implantations/mine")
    public ResponseEntity<Page<DemandeImplantationDTO>> getMine(DemandeImplantationCriteria criteria,
                                                                Pageable pageable,
                                                                @RequestParam(value = "size", required = false) Integer size,
                                                                @RequestParam(value = "masks", required = false) List<Integer> masks) {
        List<Integer> masquesEffectifs = (masks == null || masks.isEmpty()) ? List.of(1, 2, 4) : masks;
        return ResponseEntity.ok(demandeImplantationQueryService.findByCriteriaAcl(
                criteria, pageable, size, currentUser.getSid(), masquesEffectifs));
    }

    @GetMapping("/demande-implantations/{id}")
    public ResponseEntity<DemandeImplantationDTO> getOne(@PathVariable Long id) {
        return demandeImplantationService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority(@kernelService.implantation_role_canReadImplantation())")
    @GetMapping("/demande-implantation/{id}")
    public DemandeImplantationOutputDTO detail(@PathVariable Long id) {
        return demandeImplantationService.byId(id);
    }

    @DeleteMapping("/demande-implantations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        demandeImplantationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- workflow

    @PreAuthorize("hasAuthority(@kernelService.implantation_role_canCreateImplantation())")
    @PatchMapping("/firstSubmitDemandeImplantation")
    public DemandeImplantationOutputDTO firstSubmit(@RequestBody @Valid DemandeImplantationInputDTO input) throws Exception {
        // Valide AVANT aclClass() : celui-ci est un appel Feign au kernel. Sans cet
        // ordre, un dossier incomplet exigerait un aller-retour reseau pour se voir
        // refuser, et une panne du kernel masquerait la vraie raison du refus.
        demandeImplantationService.exigerPourSoumission(input);
        return demandeImplantationService.initAndSubmit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.implantation_role_canEditImplantation())")
    @PatchMapping("/submitDemandeImplantation")
    public DemandeImplantationOutputDTO submit(@RequestBody @Valid DemandeImplantationInputDTO input) throws Exception {
        demandeImplantationService.exigerPourSoumission(input);
        return demandeImplantationService.submit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.implantation_role_canEditImplantation())")
    @PatchMapping("/saveDemandeImplantationAsDraft")
    public DemandeImplantationOutputDTO saveAsDraft(@RequestBody @Valid DemandeImplantationInputDTO input) {
        return demandeImplantationService.saveAsDraft(input, aclClass());
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
    @GetMapping("/initClassDemandeImplantation")
    public void initClassDemandeImplantation() {
        kernelService.initVaraible();
        kernelService.initSequences();
        kernelService.initClassDemandeImplantation();
    }
}
