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
import picosoft.biz.arcep.domain.ussd.DemandeUssd;
import picosoft.biz.arcep.service.DemandeUssdQueryService;
import picosoft.biz.arcep.service.DemandeUssdService;
import picosoft.biz.arcep.service.criteria.DemandeUssdCriteria;
import picosoft.biz.arcep.service.dto.DemandeUssdDTO;
import picosoft.biz.arcep.service.dto.DemandeUssdInputDTO;
import picosoft.biz.arcep.service.dto.DemandeUssdOutputDTO;

import java.util.List;

/**
 * Dossier de reseau. Mince par construction : il resout l'AclClass
 * et delegue au service, comme les controleurs d'homologation.
 */
@RestController
@RequestMapping("/api")
public class DemandeUssdController {

    private final DemandeUssdService demandeUssdService;
    private final DemandeUssdQueryService demandeUssdQueryService;
    private final KernelInterface kernelInterface;
    private final KernelService kernelService;
    private final CurrentUser currentUser;

    public DemandeUssdController(DemandeUssdService demandeUssdService,
                                         DemandeUssdQueryService demandeUssdQueryService,
                                         KernelInterface kernelInterface,
                                         KernelService kernelService,
                                         CurrentUser currentUser) {
        this.demandeUssdService = demandeUssdService;
        this.demandeUssdQueryService = demandeUssdQueryService;
        this.kernelInterface = kernelInterface;
        this.kernelService = kernelService;
        this.currentUser = currentUser;
    }

    private AclClass aclClass() {
        return kernelInterface.getaclClassByClassName(DemandeUssd.class.getName());
    }

    // ------------------------------------------------------------------ CRUD

    @PostMapping("/demande-ussds")
    public ResponseEntity<DemandeUssdDTO> create(@RequestBody @Valid DemandeUssdDTO dto) {
        return ResponseEntity.ok(demandeUssdService.save(dto));
    }

    @PutMapping("/demande-ussds/{id}")
    public ResponseEntity<DemandeUssdDTO> update(@PathVariable Long id,
                                                         @RequestBody @Valid DemandeUssdDTO dto) {
        return ResponseEntity.ok(demandeUssdService.update(id, dto));
    }

    @GetMapping("/demande-ussds")
    public ResponseEntity<Page<DemandeUssdDTO>> getAll(DemandeUssdCriteria criteria,
                                                               Pageable pageable,
                                                               @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(demandeUssdService.findAll(criteria, pageable, size));
    }

    /**
     * Meme requete, restreinte aux dossiers que l'utilisateur courant a le droit de voir.
     */
    @GetMapping("/demande-ussds/mine")
    public ResponseEntity<Page<DemandeUssdDTO>> getMine(DemandeUssdCriteria criteria,
                                                                Pageable pageable,
                                                                @RequestParam(value = "size", required = false) Integer size,
                                                                @RequestParam(value = "masks", required = false) List<Integer> masks) {
        List<Integer> masquesEffectifs = (masks == null || masks.isEmpty()) ? List.of(1, 2, 4) : masks;
        return ResponseEntity.ok(demandeUssdQueryService.findByCriteriaAcl(
                criteria, pageable, size, currentUser.getSid(), masquesEffectifs));
    }

    @GetMapping("/demande-ussds/{id}")
    public ResponseEntity<DemandeUssdDTO> getOne(@PathVariable Long id) {
        return demandeUssdService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority(@kernelService.ussd_role_canReadUssd())")
    @GetMapping("/demande-ussd/{id}")
    public DemandeUssdOutputDTO detail(@PathVariable Long id) {
        return demandeUssdService.byId(id);
    }

    @DeleteMapping("/demande-ussds/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        demandeUssdService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- workflow

    @PreAuthorize("hasAuthority(@kernelService.ussd_role_canCreateUssd())")
    @PatchMapping("/firstSubmitDemandeUssd")
    public DemandeUssdOutputDTO firstSubmit(@RequestBody @Valid DemandeUssdInputDTO input) throws Exception {
        // Valide AVANT aclClass() : celui-ci est un appel Feign au kernel. Sans cet
        // ordre, un dossier incomplet exigerait un aller-retour reseau pour se voir
        // refuser, et une panne du kernel masquerait la vraie raison du refus.
        demandeUssdService.exigerPourSoumission(input);
        return demandeUssdService.initAndSubmit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.ussd_role_canEditUssd())")
    @PatchMapping("/submitDemandeUssd")
    public DemandeUssdOutputDTO submit(@RequestBody @Valid DemandeUssdInputDTO input) throws Exception {
        demandeUssdService.exigerPourSoumission(input);
        return demandeUssdService.submit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.ussd_role_canEditUssd())")
    @PatchMapping("/saveDemandeUssdAsDraft")
    public DemandeUssdOutputDTO saveAsDraft(@RequestBody @Valid DemandeUssdInputDTO input) {
        return demandeUssdService.saveAsDraft(input, aclClass());
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

    @GetMapping("/initClassDemandeUssd")
    public void initClassDemandeUssd() {
        kernelService.initVaraible();
        kernelService.initSequences();
        kernelService.initClassDemandeUssd();
    }
}
