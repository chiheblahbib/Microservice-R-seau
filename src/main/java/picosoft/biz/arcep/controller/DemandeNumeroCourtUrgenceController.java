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
import picosoft.biz.arcep.domain.numerocourturgence.DemandeNumeroCourtUrgence;
import picosoft.biz.arcep.service.DemandeNumeroCourtUrgenceQueryService;
import picosoft.biz.arcep.service.DemandeNumeroCourtUrgenceService;
import picosoft.biz.arcep.service.criteria.DemandeNumeroCourtUrgenceCriteria;
import picosoft.biz.arcep.service.dto.DemandeNumeroCourtUrgenceDTO;
import picosoft.biz.arcep.service.dto.DemandeNumeroCourtUrgenceInputDTO;
import picosoft.biz.arcep.service.dto.DemandeNumeroCourtUrgenceOutputDTO;

import java.util.List;

/**
 * Dossier de reseau. Mince par construction : il resout l'AclClass
 * et delegue au service, comme les controleurs d'homologation.
 */
@RestController
@RequestMapping("/api")
public class DemandeNumeroCourtUrgenceController {

    private final DemandeNumeroCourtUrgenceService demandeNumeroCourtUrgenceService;
    private final DemandeNumeroCourtUrgenceQueryService demandeNumeroCourtUrgenceQueryService;
    private final KernelInterface kernelInterface;
    private final KernelService kernelService;
    private final CurrentUser currentUser;

    public DemandeNumeroCourtUrgenceController(DemandeNumeroCourtUrgenceService demandeNumeroCourtUrgenceService,
                                         DemandeNumeroCourtUrgenceQueryService demandeNumeroCourtUrgenceQueryService,
                                         KernelInterface kernelInterface,
                                         KernelService kernelService,
                                         CurrentUser currentUser) {
        this.demandeNumeroCourtUrgenceService = demandeNumeroCourtUrgenceService;
        this.demandeNumeroCourtUrgenceQueryService = demandeNumeroCourtUrgenceQueryService;
        this.kernelInterface = kernelInterface;
        this.kernelService = kernelService;
        this.currentUser = currentUser;
    }

    private AclClass aclClass() {
        return kernelInterface.getaclClassByClassName(DemandeNumeroCourtUrgence.class.getName());
    }

    // ------------------------------------------------------------------ CRUD

    @PostMapping("/demande-numerocourturgences")
    public ResponseEntity<DemandeNumeroCourtUrgenceDTO> create(@RequestBody @Valid DemandeNumeroCourtUrgenceDTO dto) {
        return ResponseEntity.ok(demandeNumeroCourtUrgenceService.save(dto));
    }

    @PutMapping("/demande-numerocourturgences/{id}")
    public ResponseEntity<DemandeNumeroCourtUrgenceDTO> update(@PathVariable Long id,
                                                         @RequestBody @Valid DemandeNumeroCourtUrgenceDTO dto) {
        return ResponseEntity.ok(demandeNumeroCourtUrgenceService.update(id, dto));
    }

    @GetMapping("/demande-numerocourturgences")
    public ResponseEntity<Page<DemandeNumeroCourtUrgenceDTO>> getAll(DemandeNumeroCourtUrgenceCriteria criteria,
                                                               Pageable pageable,
                                                               @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(demandeNumeroCourtUrgenceService.findAll(criteria, pageable, size));
    }

    /**
     * Meme requete, restreinte aux dossiers que l'utilisateur courant a le droit de voir.
     */
    @GetMapping("/demande-numerocourturgences/mine")
    public ResponseEntity<Page<DemandeNumeroCourtUrgenceDTO>> getMine(DemandeNumeroCourtUrgenceCriteria criteria,
                                                                Pageable pageable,
                                                                @RequestParam(value = "size", required = false) Integer size,
                                                                @RequestParam(value = "masks", required = false) List<Integer> masks) {
        List<Integer> masquesEffectifs = (masks == null || masks.isEmpty()) ? List.of(1, 2, 4) : masks;
        return ResponseEntity.ok(demandeNumeroCourtUrgenceQueryService.findByCriteriaAcl(
                criteria, pageable, size, currentUser.getSid(), masquesEffectifs));
    }

    @GetMapping("/demande-numerocourturgences/{id}")
    public ResponseEntity<DemandeNumeroCourtUrgenceDTO> getOne(@PathVariable Long id) {
        return demandeNumeroCourtUrgenceService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority(@kernelService.numerocourturgence_role_canReadNumeroCourtUrgence())")
    @GetMapping("/demande-numerocourturgence/{id}")
    public DemandeNumeroCourtUrgenceOutputDTO detail(@PathVariable Long id) {
        return demandeNumeroCourtUrgenceService.byId(id);
    }

    @PreAuthorize("hasAuthority(@kernelService.numerocourturgence_role_canEditNumeroCourtUrgence())")
    @DeleteMapping("/demande-numerocourturgences/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        demandeNumeroCourtUrgenceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- workflow

    @PreAuthorize("hasAuthority(@kernelService.numerocourturgence_role_canCreateNumeroCourtUrgence())")
    @PatchMapping("/firstSubmitDemandeNumeroCourtUrgence")
    public DemandeNumeroCourtUrgenceOutputDTO firstSubmit(@RequestBody @Valid DemandeNumeroCourtUrgenceInputDTO input) throws Exception {
        // Valide AVANT aclClass() : celui-ci est un appel Feign au kernel. Sans cet
        // ordre, un dossier incomplet exigerait un aller-retour reseau pour se voir
        // refuser, et une panne du kernel masquerait la vraie raison du refus.
        demandeNumeroCourtUrgenceService.exigerPourSoumission(input);
        return demandeNumeroCourtUrgenceService.initAndSubmit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.numerocourturgence_role_canEditNumeroCourtUrgence())")
    @PatchMapping("/submitDemandeNumeroCourtUrgence")
    public DemandeNumeroCourtUrgenceOutputDTO submit(@RequestBody @Valid DemandeNumeroCourtUrgenceInputDTO input) throws Exception {
        demandeNumeroCourtUrgenceService.exigerPourSoumission(input);
        return demandeNumeroCourtUrgenceService.submit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.numerocourturgence_role_canEditNumeroCourtUrgence())")
    @PatchMapping("/saveDemandeNumeroCourtUrgenceAsDraft")
    public DemandeNumeroCourtUrgenceOutputDTO saveAsDraft(@RequestBody @Valid DemandeNumeroCourtUrgenceInputDTO input) {
        return demandeNumeroCourtUrgenceService.saveAsDraft(input, aclClass());
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

    @GetMapping("/initClassDemandeNumeroCourtUrgence")
    public void initClassDemandeNumeroCourtUrgence() {
        kernelService.initVaraible();
        kernelService.initSequences();
        kernelService.initClassDemandeNumeroCourtUrgence();
    }
}
