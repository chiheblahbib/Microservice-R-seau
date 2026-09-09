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
import picosoft.biz.arcep.domain.aeronef.DemandeAeronef;
import picosoft.biz.arcep.service.DemandeAeronefQueryService;
import picosoft.biz.arcep.service.DemandeAeronefService;
import picosoft.biz.arcep.service.criteria.DemandeAeronefCriteria;
import picosoft.biz.arcep.service.dto.DemandeAeronefDTO;
import picosoft.biz.arcep.service.dto.DemandeAeronefInputDTO;
import picosoft.biz.arcep.service.dto.DemandeAeronefOutputDTO;

import java.util.List;

/**
 * Dossier de reseau. Mince par construction : il resout l'AclClass
 * et delegue au service, comme les controleurs d'homologation.
 */
@RestController
@RequestMapping("/api")
public class DemandeAeronefController {

    private final DemandeAeronefService demandeAeronefService;
    private final DemandeAeronefQueryService demandeAeronefQueryService;
    private final KernelInterface kernelInterface;
    private final KernelService kernelService;
    private final CurrentUser currentUser;

    public DemandeAeronefController(DemandeAeronefService demandeAeronefService,
                                         DemandeAeronefQueryService demandeAeronefQueryService,
                                         KernelInterface kernelInterface,
                                         KernelService kernelService,
                                         CurrentUser currentUser) {
        this.demandeAeronefService = demandeAeronefService;
        this.demandeAeronefQueryService = demandeAeronefQueryService;
        this.kernelInterface = kernelInterface;
        this.kernelService = kernelService;
        this.currentUser = currentUser;
    }

    private AclClass aclClass() {
        return kernelInterface.getaclClassByClassName(DemandeAeronef.class.getName());
    }

    // ------------------------------------------------------------------ CRUD

    @PostMapping("/demande-aeronefs")
    public ResponseEntity<DemandeAeronefDTO> create(@RequestBody @Valid DemandeAeronefDTO dto) {
        return ResponseEntity.ok(demandeAeronefService.save(dto));
    }

    @PutMapping("/demande-aeronefs/{id}")
    public ResponseEntity<DemandeAeronefDTO> update(@PathVariable Long id,
                                                         @RequestBody @Valid DemandeAeronefDTO dto) {
        return ResponseEntity.ok(demandeAeronefService.update(id, dto));
    }

    @GetMapping("/demande-aeronefs")
    public ResponseEntity<Page<DemandeAeronefDTO>> getAll(DemandeAeronefCriteria criteria,
                                                               Pageable pageable,
                                                               @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(demandeAeronefService.findAll(criteria, pageable, size));
    }

    /**
     * Meme requete, restreinte aux dossiers que l'utilisateur courant a le droit de voir.
     */
    @GetMapping("/demande-aeronefs/mine")
    public ResponseEntity<Page<DemandeAeronefDTO>> getMine(DemandeAeronefCriteria criteria,
                                                                Pageable pageable,
                                                                @RequestParam(value = "size", required = false) Integer size,
                                                                @RequestParam(value = "masks", required = false) List<Integer> masks) {
        List<Integer> masquesEffectifs = (masks == null || masks.isEmpty()) ? List.of(1, 2, 4) : masks;
        return ResponseEntity.ok(demandeAeronefQueryService.findByCriteriaAcl(
                criteria, pageable, size, currentUser.getSid(), masquesEffectifs));
    }

    @GetMapping("/demande-aeronefs/{id}")
    public ResponseEntity<DemandeAeronefDTO> getOne(@PathVariable Long id) {
        return demandeAeronefService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority(@kernelService.aeronef_role_canReadAeronef())")
    @GetMapping("/demande-aeronef/{id}")
    public DemandeAeronefOutputDTO detail(@PathVariable Long id) {
        return demandeAeronefService.byId(id);
    }

    @DeleteMapping("/demande-aeronefs/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        demandeAeronefService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- workflow

    @PreAuthorize("hasAuthority(@kernelService.aeronef_role_canCreateAeronef())")
    @PatchMapping("/firstSubmitDemandeAeronef")
    public DemandeAeronefOutputDTO firstSubmit(@RequestBody @Valid DemandeAeronefInputDTO input) throws Exception {
        // Valide AVANT aclClass() : celui-ci est un appel Feign au kernel. Sans cet
        // ordre, un dossier incomplet exigerait un aller-retour reseau pour se voir
        // refuser, et une panne du kernel masquerait la vraie raison du refus.
        demandeAeronefService.exigerPourSoumission(input);
        return demandeAeronefService.initAndSubmit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.aeronef_role_canEditAeronef())")
    @PatchMapping("/submitDemandeAeronef")
    public DemandeAeronefOutputDTO submit(@RequestBody @Valid DemandeAeronefInputDTO input) throws Exception {
        demandeAeronefService.exigerPourSoumission(input);
        return demandeAeronefService.submit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.aeronef_role_canEditAeronef())")
    @PatchMapping("/saveDemandeAeronefAsDraft")
    public DemandeAeronefOutputDTO saveAsDraft(@RequestBody @Valid DemandeAeronefInputDTO input) {
        return demandeAeronefService.saveAsDraft(input, aclClass());
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

    @GetMapping("/initClassDemandeAeronef")
    public void initClassDemandeAeronef() {
        kernelService.initVaraible();
        kernelService.initSequences();
        kernelService.initClassDemandeAeronef();
    }
}
