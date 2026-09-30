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
import picosoft.biz.arcep.domain.mmsi.DemandeMmsi;
import picosoft.biz.arcep.service.DemandeMmsiQueryService;
import picosoft.biz.arcep.service.DemandeMmsiService;
import picosoft.biz.arcep.service.criteria.DemandeMmsiCriteria;
import picosoft.biz.arcep.service.dto.DemandeMmsiDTO;
import picosoft.biz.arcep.service.dto.DemandeMmsiInputDTO;
import picosoft.biz.arcep.service.dto.DemandeMmsiOutputDTO;

import java.util.List;

/**
 * Dossier de reseau. Mince par construction : il resout l'AclClass
 * et delegue au service, comme les controleurs d'homologation.
 */
@RestController
@RequestMapping("/api")
public class DemandeMmsiController {

    private final DemandeMmsiService demandeMmsiService;
    private final DemandeMmsiQueryService demandeMmsiQueryService;
    private final KernelInterface kernelInterface;
    private final KernelService kernelService;
    private final CurrentUser currentUser;

    public DemandeMmsiController(DemandeMmsiService demandeMmsiService,
                                         DemandeMmsiQueryService demandeMmsiQueryService,
                                         KernelInterface kernelInterface,
                                         KernelService kernelService,
                                         CurrentUser currentUser) {
        this.demandeMmsiService = demandeMmsiService;
        this.demandeMmsiQueryService = demandeMmsiQueryService;
        this.kernelInterface = kernelInterface;
        this.kernelService = kernelService;
        this.currentUser = currentUser;
    }

    private AclClass aclClass() {
        return kernelInterface.getaclClassByClassName(DemandeMmsi.class.getName());
    }

    // ------------------------------------------------------------------ CRUD

    @PostMapping("/demande-mmsis")
    public ResponseEntity<DemandeMmsiDTO> create(@RequestBody @Valid DemandeMmsiDTO dto) {
        return ResponseEntity.ok(demandeMmsiService.save(dto));
    }

    @PutMapping("/demande-mmsis/{id}")
    public ResponseEntity<DemandeMmsiDTO> update(@PathVariable Long id,
                                                         @RequestBody @Valid DemandeMmsiDTO dto) {
        return ResponseEntity.ok(demandeMmsiService.update(id, dto));
    }

    @GetMapping("/demande-mmsis")
    public ResponseEntity<Page<DemandeMmsiDTO>> getAll(DemandeMmsiCriteria criteria,
                                                               Pageable pageable,
                                                               @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(demandeMmsiService.findAll(criteria, pageable, size));
    }

    /**
     * Meme requete, restreinte aux dossiers que l'utilisateur courant a le droit de voir.
     */
    @GetMapping("/demande-mmsis/mine")
    public ResponseEntity<Page<DemandeMmsiDTO>> getMine(DemandeMmsiCriteria criteria,
                                                                Pageable pageable,
                                                                @RequestParam(value = "size", required = false) Integer size,
                                                                @RequestParam(value = "masks", required = false) List<Integer> masks) {
        List<Integer> masquesEffectifs = (masks == null || masks.isEmpty()) ? List.of(1, 2, 4) : masks;
        return ResponseEntity.ok(demandeMmsiQueryService.findByCriteriaAcl(
                criteria, pageable, size, currentUser.getSid(), masquesEffectifs));
    }

    @GetMapping("/demande-mmsis/{id}")
    public ResponseEntity<DemandeMmsiDTO> getOne(@PathVariable Long id) {
        return demandeMmsiService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority(@kernelService.mmsi_role_canReadMmsi())")
    @GetMapping("/demande-mmsi/{id}")
    public DemandeMmsiOutputDTO detail(@PathVariable Long id) {
        return demandeMmsiService.byId(id);
    }

    @PreAuthorize("hasAuthority(@kernelService.mmsi_role_canEditMmsi())")
    @DeleteMapping("/demande-mmsis/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        demandeMmsiService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- workflow

    @PreAuthorize("hasAuthority(@kernelService.mmsi_role_canCreateMmsi())")
    @PatchMapping("/firstSubmitDemandeMmsi")
    public DemandeMmsiOutputDTO firstSubmit(@RequestBody @Valid DemandeMmsiInputDTO input) throws Exception {
        // Valide AVANT aclClass() : celui-ci est un appel Feign au kernel. Sans cet
        // ordre, un dossier incomplet exigerait un aller-retour reseau pour se voir
        // refuser, et une panne du kernel masquerait la vraie raison du refus.
        demandeMmsiService.exigerPourSoumission(input);
        return demandeMmsiService.initAndSubmit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.mmsi_role_canEditMmsi())")
    @PatchMapping("/submitDemandeMmsi")
    public DemandeMmsiOutputDTO submit(@RequestBody @Valid DemandeMmsiInputDTO input) throws Exception {
        demandeMmsiService.exigerPourSoumission(input);
        return demandeMmsiService.submit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.mmsi_role_canEditMmsi())")
    @PatchMapping("/saveDemandeMmsiAsDraft")
    public DemandeMmsiOutputDTO saveAsDraft(@RequestBody @Valid DemandeMmsiInputDTO input) {
        return demandeMmsiService.saveAsDraft(input, aclClass());
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

    @GetMapping("/initClassDemandeMmsi")
    public void initClassDemandeMmsi() {
        kernelService.initVaraible();
        kernelService.initSequences();
        kernelService.initClassDemandeMmsi();
    }
}
