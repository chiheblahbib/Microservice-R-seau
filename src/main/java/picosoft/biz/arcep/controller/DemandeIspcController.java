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
import picosoft.biz.arcep.domain.ispc.DemandeIspc;
import picosoft.biz.arcep.service.DemandeIspcQueryService;
import picosoft.biz.arcep.service.DemandeIspcService;
import picosoft.biz.arcep.service.criteria.DemandeIspcCriteria;
import picosoft.biz.arcep.service.dto.DemandeIspcDTO;
import picosoft.biz.arcep.service.dto.DemandeIspcInputDTO;
import picosoft.biz.arcep.service.dto.DemandeIspcOutputDTO;

import java.util.List;

/**
 * Dossier de reseau. Mince par construction : il resout l'AclClass
 * et delegue au service, comme les controleurs d'homologation.
 */
@RestController
@RequestMapping("/api")
public class DemandeIspcController {

    private final DemandeIspcService demandeIspcService;
    private final DemandeIspcQueryService demandeIspcQueryService;
    private final KernelInterface kernelInterface;
    private final KernelService kernelService;
    private final CurrentUser currentUser;

    public DemandeIspcController(DemandeIspcService demandeIspcService,
                                         DemandeIspcQueryService demandeIspcQueryService,
                                         KernelInterface kernelInterface,
                                         KernelService kernelService,
                                         CurrentUser currentUser) {
        this.demandeIspcService = demandeIspcService;
        this.demandeIspcQueryService = demandeIspcQueryService;
        this.kernelInterface = kernelInterface;
        this.kernelService = kernelService;
        this.currentUser = currentUser;
    }

    private AclClass aclClass() {
        return kernelInterface.getaclClassByClassName(DemandeIspc.class.getName());
    }

    // ------------------------------------------------------------------ CRUD

    @PostMapping("/demande-ispcs")
    public ResponseEntity<DemandeIspcDTO> create(@RequestBody @Valid DemandeIspcDTO dto) {
        return ResponseEntity.ok(demandeIspcService.save(dto));
    }

    @PutMapping("/demande-ispcs/{id}")
    public ResponseEntity<DemandeIspcDTO> update(@PathVariable Long id,
                                                         @RequestBody @Valid DemandeIspcDTO dto) {
        return ResponseEntity.ok(demandeIspcService.update(id, dto));
    }

    @GetMapping("/demande-ispcs")
    public ResponseEntity<Page<DemandeIspcDTO>> getAll(DemandeIspcCriteria criteria,
                                                               Pageable pageable,
                                                               @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(demandeIspcService.findAll(criteria, pageable, size));
    }

    /**
     * Meme requete, restreinte aux dossiers que l'utilisateur courant a le droit de voir.
     */
    @GetMapping("/demande-ispcs/mine")
    public ResponseEntity<Page<DemandeIspcDTO>> getMine(DemandeIspcCriteria criteria,
                                                                Pageable pageable,
                                                                @RequestParam(value = "size", required = false) Integer size,
                                                                @RequestParam(value = "masks", required = false) List<Integer> masks) {
        List<Integer> masquesEffectifs = (masks == null || masks.isEmpty()) ? List.of(1, 2, 4) : masks;
        return ResponseEntity.ok(demandeIspcQueryService.findByCriteriaAcl(
                criteria, pageable, size, currentUser.getSid(), masquesEffectifs));
    }

    @GetMapping("/demande-ispcs/{id}")
    public ResponseEntity<DemandeIspcDTO> getOne(@PathVariable Long id) {
        return demandeIspcService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority(@kernelService.ispc_role_canReadIspc())")
    @GetMapping("/demande-ispc/{id}")
    public DemandeIspcOutputDTO detail(@PathVariable Long id) {
        return demandeIspcService.byId(id);
    }

    @PreAuthorize("hasAuthority(@kernelService.ispc_role_canEditIspc())")
    @DeleteMapping("/demande-ispcs/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        demandeIspcService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- workflow

    @PreAuthorize("hasAuthority(@kernelService.ispc_role_canCreateIspc())")
    @PatchMapping("/firstSubmitDemandeIspc")
    public DemandeIspcOutputDTO firstSubmit(@RequestBody @Valid DemandeIspcInputDTO input) throws Exception {
        // Valide AVANT aclClass() : celui-ci est un appel Feign au kernel. Sans cet
        // ordre, un dossier incomplet exigerait un aller-retour reseau pour se voir
        // refuser, et une panne du kernel masquerait la vraie raison du refus.
        demandeIspcService.exigerPourSoumission(input);
        return demandeIspcService.initAndSubmit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.ispc_role_canEditIspc())")
    @PatchMapping("/submitDemandeIspc")
    public DemandeIspcOutputDTO submit(@RequestBody @Valid DemandeIspcInputDTO input) throws Exception {
        demandeIspcService.exigerPourSoumission(input);
        return demandeIspcService.submit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.ispc_role_canEditIspc())")
    @PatchMapping("/saveDemandeIspcAsDraft")
    public DemandeIspcOutputDTO saveAsDraft(@RequestBody @Valid DemandeIspcInputDTO input) {
        return demandeIspcService.saveAsDraft(input, aclClass());
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

    @GetMapping("/initClassDemandeIspc")
    public void initClassDemandeIspc() {
        kernelService.initVaraible();
        kernelService.initSequences();
        kernelService.initClassDemandeIspc();
    }
}
