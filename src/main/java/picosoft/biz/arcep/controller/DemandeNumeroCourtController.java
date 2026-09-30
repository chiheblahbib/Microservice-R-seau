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
import picosoft.biz.arcep.domain.numerocourt.DemandeNumeroCourt;
import picosoft.biz.arcep.service.DemandeNumeroCourtQueryService;
import picosoft.biz.arcep.service.DemandeNumeroCourtService;
import picosoft.biz.arcep.service.criteria.DemandeNumeroCourtCriteria;
import picosoft.biz.arcep.service.dto.DemandeNumeroCourtDTO;
import picosoft.biz.arcep.service.dto.DemandeNumeroCourtInputDTO;
import picosoft.biz.arcep.service.dto.DemandeNumeroCourtOutputDTO;

import java.util.List;

/**
 * Dossier de reseau. Mince par construction : il resout l'AclClass
 * et delegue au service, comme les controleurs d'homologation.
 */
@RestController
@RequestMapping("/api")
public class DemandeNumeroCourtController {

    private final DemandeNumeroCourtService demandeNumeroCourtService;
    private final DemandeNumeroCourtQueryService demandeNumeroCourtQueryService;
    private final KernelInterface kernelInterface;
    private final KernelService kernelService;
    private final CurrentUser currentUser;

    public DemandeNumeroCourtController(DemandeNumeroCourtService demandeNumeroCourtService,
                                         DemandeNumeroCourtQueryService demandeNumeroCourtQueryService,
                                         KernelInterface kernelInterface,
                                         KernelService kernelService,
                                         CurrentUser currentUser) {
        this.demandeNumeroCourtService = demandeNumeroCourtService;
        this.demandeNumeroCourtQueryService = demandeNumeroCourtQueryService;
        this.kernelInterface = kernelInterface;
        this.kernelService = kernelService;
        this.currentUser = currentUser;
    }

    private AclClass aclClass() {
        return kernelInterface.getaclClassByClassName(DemandeNumeroCourt.class.getName());
    }

    // ------------------------------------------------------------------ CRUD

    @PostMapping("/demande-numerocourts")
    public ResponseEntity<DemandeNumeroCourtDTO> create(@RequestBody @Valid DemandeNumeroCourtDTO dto) {
        return ResponseEntity.ok(demandeNumeroCourtService.save(dto));
    }

    @PutMapping("/demande-numerocourts/{id}")
    public ResponseEntity<DemandeNumeroCourtDTO> update(@PathVariable Long id,
                                                         @RequestBody @Valid DemandeNumeroCourtDTO dto) {
        return ResponseEntity.ok(demandeNumeroCourtService.update(id, dto));
    }

    @GetMapping("/demande-numerocourts")
    public ResponseEntity<Page<DemandeNumeroCourtDTO>> getAll(DemandeNumeroCourtCriteria criteria,
                                                               Pageable pageable,
                                                               @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(demandeNumeroCourtService.findAll(criteria, pageable, size));
    }

    /**
     * Meme requete, restreinte aux dossiers que l'utilisateur courant a le droit de voir.
     */
    @GetMapping("/demande-numerocourts/mine")
    public ResponseEntity<Page<DemandeNumeroCourtDTO>> getMine(DemandeNumeroCourtCriteria criteria,
                                                                Pageable pageable,
                                                                @RequestParam(value = "size", required = false) Integer size,
                                                                @RequestParam(value = "masks", required = false) List<Integer> masks) {
        List<Integer> masquesEffectifs = (masks == null || masks.isEmpty()) ? List.of(1, 2, 4) : masks;
        return ResponseEntity.ok(demandeNumeroCourtQueryService.findByCriteriaAcl(
                criteria, pageable, size, currentUser.getSid(), masquesEffectifs));
    }

    @GetMapping("/demande-numerocourts/{id}")
    public ResponseEntity<DemandeNumeroCourtDTO> getOne(@PathVariable Long id) {
        return demandeNumeroCourtService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority(@kernelService.numerocourt_role_canReadNumeroCourt())")
    @GetMapping("/demande-numerocourt/{id}")
    public DemandeNumeroCourtOutputDTO detail(@PathVariable Long id) {
        return demandeNumeroCourtService.byId(id);
    }

    @PreAuthorize("hasAuthority(@kernelService.numerocourt_role_canEditNumeroCourt())")
    @DeleteMapping("/demande-numerocourts/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        demandeNumeroCourtService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- workflow

    @PreAuthorize("hasAuthority(@kernelService.numerocourt_role_canCreateNumeroCourt())")
    @PatchMapping("/firstSubmitDemandeNumeroCourt")
    public DemandeNumeroCourtOutputDTO firstSubmit(@RequestBody @Valid DemandeNumeroCourtInputDTO input) throws Exception {
        // Valide AVANT aclClass() : celui-ci est un appel Feign au kernel. Sans cet
        // ordre, un dossier incomplet exigerait un aller-retour reseau pour se voir
        // refuser, et une panne du kernel masquerait la vraie raison du refus.
        demandeNumeroCourtService.exigerPourSoumission(input);
        return demandeNumeroCourtService.initAndSubmit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.numerocourt_role_canEditNumeroCourt())")
    @PatchMapping("/submitDemandeNumeroCourt")
    public DemandeNumeroCourtOutputDTO submit(@RequestBody @Valid DemandeNumeroCourtInputDTO input) throws Exception {
        demandeNumeroCourtService.exigerPourSoumission(input);
        return demandeNumeroCourtService.submit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.numerocourt_role_canEditNumeroCourt())")
    @PatchMapping("/saveDemandeNumeroCourtAsDraft")
    public DemandeNumeroCourtOutputDTO saveAsDraft(@RequestBody @Valid DemandeNumeroCourtInputDTO input) {
        return demandeNumeroCourtService.saveAsDraft(input, aclClass());
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

    @GetMapping("/initClassDemandeNumeroCourt")
    public void initClassDemandeNumeroCourt() {
        kernelService.initVaraible();
        kernelService.initSequences();
        kernelService.initClassDemandeNumeroCourt();
    }
}
