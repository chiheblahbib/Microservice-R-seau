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
import picosoft.biz.arcep.domain.implantation.Station;
import picosoft.biz.arcep.service.StationQueryService;
import picosoft.biz.arcep.service.StationService;
import picosoft.biz.arcep.service.criteria.StationCriteria;
import picosoft.biz.arcep.service.dto.StationDTO;
import picosoft.biz.arcep.service.dto.StationInputDTO;
import picosoft.biz.arcep.service.dto.StationOutputDTO;

import java.util.List;

/**
 * Station, qui porte son propre circuit (processStation).
 */
@RestController
@RequestMapping("/api")
public class StationController {

    private final StationService stationService;
    private final StationQueryService stationQueryService;
    private final KernelInterface kernelInterface;
    private final KernelService kernelService;
    private final CurrentUser currentUser;

    public StationController(StationService stationService,
                             StationQueryService stationQueryService,
                             KernelInterface kernelInterface,
                             KernelService kernelService,
                             CurrentUser currentUser) {
        this.stationService = stationService;
        this.stationQueryService = stationQueryService;
        this.kernelInterface = kernelInterface;
        this.kernelService = kernelService;
        this.currentUser = currentUser;
    }

    private AclClass aclClass() {
        return kernelInterface.getaclClassByClassName(Station.class.getName());
    }

    // ------------------------------------------------------------------ CRUD

    @PostMapping("/stations")
    public ResponseEntity<StationDTO> create(@RequestBody @Valid StationDTO dto) {
        return ResponseEntity.ok(stationService.save(dto));
    }

    @PutMapping("/stations/{id}")
    public ResponseEntity<StationDTO> update(@PathVariable Long id, @RequestBody @Valid StationDTO dto) {
        return ResponseEntity.ok(stationService.update(id, dto));
    }

    @GetMapping("/stations")
    public ResponseEntity<Page<StationDTO>> getAll(StationCriteria criteria, Pageable pageable,
                                                   @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(stationService.findAll(criteria, pageable, size));
    }

    @GetMapping("/stations/mine")
    public ResponseEntity<Page<StationDTO>> getMine(StationCriteria criteria, Pageable pageable,
                                                    @RequestParam(value = "size", required = false) Integer size,
                                                    @RequestParam(value = "masks", required = false) List<Integer> masks) {
        List<Integer> masquesEffectifs = (masks == null || masks.isEmpty()) ? List.of(1, 2, 4) : masks;
        return ResponseEntity.ok(stationQueryService.findByCriteriaAcl(
                criteria, pageable, size, currentUser.getSid(), masquesEffectifs));
    }

    @GetMapping("/stations/{id}")
    public ResponseEntity<StationDTO> getOne(@PathVariable Long id) {
        return stationService.findOne(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/demande-implantations/{demandeImplantationId}/stations")
    public ResponseEntity<List<StationDTO>> getByDemande(@PathVariable Long demandeImplantationId) {
        return ResponseEntity.ok(stationService.findByDemandeImplantation(demandeImplantationId));
    }

    @PreAuthorize("hasAuthority(@kernelService.station_role_canReadStation())")
    @GetMapping("/station/{id}")
    public StationOutputDTO detail(@PathVariable Long id) {
        return stationService.byId(id);
    }

    @DeleteMapping("/stations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------- workflow

    @PreAuthorize("hasAuthority(@kernelService.station_role_canCreateStation())")
    @PatchMapping("/firstSubmitStation")
    public StationOutputDTO firstSubmit(@RequestBody @Valid StationInputDTO input) throws Exception {
        return stationService.initAndSubmit(input, aclClass());
    }

    @PreAuthorize("hasAuthority(@kernelService.station_role_canEditStation())")
    @PatchMapping("/submitStation")
    public StationOutputDTO submit(@RequestBody @Valid StationInputDTO input) throws Exception {
        return stationService.submit(input, aclClass());
    }

    /**
     * Eclatement : demarre un circuit enfant par station du dossier parent.
     * Le pendant de initHomologationFromAsi.
     */
    @PostMapping("/initStationsFromDemande/{demandeImplantationId}")
    public ResponseEntity<List<StationOutputDTO>> initFromDemande(@PathVariable Long demandeImplantationId) throws Exception {
        return ResponseEntity.ok(stationService.initFromDemande(demandeImplantationId, aclClass()));
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
     * Declare la classe ACL de la STATION.
     *
     * Elle a sa propre classe et son propre circuit : l'autorisation est
     * delivree par station, et le dossier parent eclate en circuits enfants
     * a la sortie de l'etude technique.
     */
    @GetMapping("/initClassStation")
    public void initClassStation() {
        kernelService.initVaraible();
        kernelService.initSequences();
        kernelService.initClassStation();
    }
}
