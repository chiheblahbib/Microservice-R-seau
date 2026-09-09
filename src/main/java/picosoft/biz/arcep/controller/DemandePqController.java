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
import picosoft.biz.arcep.domain.pq.DemandePq;
import picosoft.biz.arcep.service.DemandePqQueryService;
import picosoft.biz.arcep.service.DemandePqService;
import picosoft.biz.arcep.service.criteria.DemandePqCriteria;
import picosoft.biz.arcep.service.dto.DemandePqDTO;
import picosoft.biz.arcep.service.dto.DemandePqInputDTO;
import picosoft.biz.arcep.service.dto.DemandePqOutputDTO;

import java.util.List;

/**
 * Dossier de reseau. Mince par construction : il resout l'AclClass
 * et delegue au service, comme les controleurs d'homologation.
 */
@RestController
@RequestMapping("/api")
public class DemandePqController {

    private final DemandePqService demandePqService;
    private final DemandePqQueryService demandePqQueryService;
    private final KernelInterface kernelInterface;
    private final KernelService kernelService;
    private final CurrentUser currentUser;

    public DemandePqController(DemandePqService demandePqService,
                                         DemandePqQueryService demandePqQueryService,
                                         KernelInterface kernelInterface,
                                         KernelService kernelService,
                                         CurrentUser currentUser) {
        this.demandePqService = demandePqService;
        this.demandePqQueryService = demandePqQueryService;
        this.kernelInterface = kernelInterface;
        this.kernelService = kernelService;
        this.currentUser = currentUser;
    }

    private AclClass aclClass() {
        return kernelInterface.getaclClassByClassName(DemandePq.class.getName());
    }

    // ------------------------------------------------------------------ CRUD

    @PostMapping("/demande-pqs")
    public ResponseEntity<DemandePqDTO> create(@RequestBody @Valid DemandePqDTO dto) {
        return ResponseEntity.ok(demandePqService.save(dto));
    }

    @PutMapping("/demande-pqs/{id}")
    public ResponseEntity<DemandePqDTO> update(@PathVariable Long id,
                                                         @RequestBody @Valid DemandePqDTO dto) {
        return ResponseEntity.ok(demandePqService.update(id, dto));
    }

    @GetMapping("/demande-pqs")
    public ResponseEntity<Page<DemandePqDTO>> getAll(DemandePqCriteria criteria,
                                                               Pageable pageable,
                                                               @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(demandePqService.findAll(criteria, pageable, size));
    }

    /**
     * Meme requete, restreinte aux dossiers que l'utilisateur courant a le droit de voir.
     */
    @GetMapping("/demande-pqs/mine")
    public ResponseEntity<Page<DemandePqDTO>> getMine(DemandePqCriteria criteria,
                                                                Pageable pageable,
                                                                @RequestParam(value = "size", required = false) Integer size,
                                                                @RequestParam(value = "masks", required = false) List<Integer> masks) {
        List<Integer> masquesEffectifs = (masks == null || masks.isEmpty()) ? List.of(1, 2, 4) : masks;
        return ResponseEntity.ok(demandePqQueryService.findByCriteriaAcl(
                criteria, pageable, size, currentUser.getSid(), masquesEffectifs));
    }

    @GetMapping("/demande-pqs/{id}")
    public ResponseEntity<DemandePqDTO> getOne(@PathVariable Long id) {
        return demandePqService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority(@kernelService.pq_role_canReadPq())")
    @GetMapping("/demande-pq/{id}")
    public DemandePqOutputDTO detail(@PathVariable Long id) {
        return demandePqService.byId(id);
    }

    @DeleteMapping("/demande-pqs/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        demandePqService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- workflow

    @PreAuthorize("hasAuthority(@kernelService.pq_role_canCreatePq())")
    @PatchMapping("/firstSubmitDemandePq")
    public DemandePqOutputDTO firstSubmit(@RequestBody @Valid DemandePqInputDTO input) throws Exception {
        // Valide AVANT aclClass() : celui-ci est un appel Feign au kernel. Sans cet
        // ordre, un dossier incomplet exigerait un aller-retour reseau pour se voir
        // refuser, et une panne du kernel masquerait la vraie raison du refus.
        demandePqService.exigerPourSoumission(input);
        return demandePqService.initAndSubmit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.pq_role_canEditPq())")
    @PatchMapping("/submitDemandePq")
    public DemandePqOutputDTO submit(@RequestBody @Valid DemandePqInputDTO input) throws Exception {
        demandePqService.exigerPourSoumission(input);
        return demandePqService.submit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.pq_role_canEditPq())")
    @PatchMapping("/saveDemandePqAsDraft")
    public DemandePqOutputDTO saveAsDraft(@RequestBody @Valid DemandePqInputDTO input) {
        return demandePqService.saveAsDraft(input, aclClass());
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

    @GetMapping("/initClassDemandePq")
    public void initClassDemandePq() {
        kernelService.initVaraible();
        kernelService.initSequences();
        kernelService.initClassDemandePq();
    }
}
