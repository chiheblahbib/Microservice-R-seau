package picosoft.biz.arcep.controller;

import io.github.jhipster.web.util.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import picosoft.biz.arcep.client.currentuser.model.CurrentUser;
import picosoft.biz.arcep.client.kernel.intercomm.KernelInterface;
import picosoft.biz.arcep.client.kernel.intercomm.KernelService;
import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import picosoft.biz.arcep.client.referentiel.ReferentielInterface;
import picosoft.biz.arcep.domain.shared.DemandeComplement;
import picosoft.biz.arcep.service.DemandeComplementQueryService;
import picosoft.biz.arcep.service.DemandeComplementService;
import picosoft.biz.arcep.service.criteria.DemandeComplementCriteria;
import picosoft.biz.arcep.service.dto.DemandeComplementDTO;
import picosoft.biz.arcep.service.dto.DemandeComplementInputDTO;
import picosoft.biz.arcep.service.dto.DemandeComplementOutputDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class DemandeComplementController {

    private final DemandeComplementService demandeComplementService;
    private final DemandeComplementQueryService demandeComplementQueryService;
    private final KernelInterface kernelInterface;
    private final KernelService kernelService;
    private final CurrentUser currentUser;
    private final ReferentielInterface referentielInterface;

    public DemandeComplementController(DemandeComplementService demandeComplementService,
                                       DemandeComplementQueryService demandeComplementQueryService,
                                       KernelInterface kernelInterface,
                                       KernelService kernelService,
                                       CurrentUser currentUser,
                                       ReferentielInterface referentielInterface) {
        this.demandeComplementService = demandeComplementService;
        this.demandeComplementQueryService = demandeComplementQueryService;
        this.kernelInterface = kernelInterface;
        this.kernelService = kernelService;
        this.currentUser = currentUser;
        this.referentielInterface = referentielInterface;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        if (binder.getTarget() instanceof DemandeComplementCriteria) {
            binder.setDisallowedFields("search");
        }
    }

    @PostMapping("/demande-complements")
    public ResponseEntity<DemandeComplementDTO> createDemandeComplement(@RequestBody DemandeComplementDTO demandeComplementDTO) {
        DemandeComplementDTO result = demandeComplementService.save(demandeComplementDTO);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/demande-complements/{id}")
    public ResponseEntity<DemandeComplementDTO> updateDemandeComplement(@PathVariable Long id, @RequestBody DemandeComplementDTO demandeComplementDTO) {
        DemandeComplementDTO result = demandeComplementService.update(id, demandeComplementDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/demande-complements")
    public ResponseEntity<Page<DemandeComplementDTO>> getAllDemandeComplements(Pageable pageable) {
        Page<DemandeComplementDTO> page = demandeComplementService.findAll(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/findAllDemandeComplements")
    public ResponseEntity<Page<DemandeComplementDTO>> findAllDemandeComplements(DemandeComplementCriteria criteria, Pageable pageable, @RequestParam(value = "size", required = false) Integer size, @RequestParam(value = "search", required = false) String search) {
        if (search != null && !search.isBlank()) {
            io.github.jhipster.service.filter.StringFilter sf = new io.github.jhipster.service.filter.StringFilter();
            sf.setContains(search.trim());
            criteria.setSearch(sf);
        }
        List<Integer> masks = new ArrayList<>();
        masks.add(1);
        masks.add(2);
        masks.add(4);
        List<String> sids = currentUser.getSid();

        Page<DemandeComplementDTO> page = demandeComplementQueryService.findByCriteriaAcl(criteria, pageable, size, sids, masks);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page);
    }

    @GetMapping("/demande-complements/{id}")
    public ResponseEntity<DemandeComplementOutputDTO> getDemandeComplement(@PathVariable Long id) {
        DemandeComplementOutputDTO dto = demandeComplementService.findOneWithPermission(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/demande-complements/asi/{asiId}")
    public ResponseEntity<List<DemandeComplementDTO>> getDemandeComplementsByAsi(@PathVariable Long asiId) {
        List<DemandeComplementDTO> list = demandeComplementService.findByAsiId(asiId);
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/demande-complements/{id}")
    public ResponseEntity<Void> deleteDemandeComplement(@PathVariable Long id) {
        demandeComplementService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority(@kernelService.demandeComplement_role_canCreateDemandeComplement())")
    @PatchMapping("/saveDraftDemandeComplement")
    public DemandeComplementOutputDTO saveDraftDemandeComplement(@RequestBody DemandeComplementInputDTO inputDTO) throws Exception {
        AclClass aclClass = kernelInterface.getaclClassByClassName(DemandeComplement.class.getName());
        return demandeComplementService.saveDraftDemandeComplement(inputDTO, aclClass);
    }

    @PreAuthorize("hasAuthority(@kernelService.demandeComplement_role_canCreateDemandeComplement())")
    @PatchMapping("/firstSubmitDemandeComplement")
    public DemandeComplementOutputDTO firstSubmitDemandeComplement(@RequestBody DemandeComplementInputDTO inputDTO) throws Exception {
        AclClass aclClass = kernelInterface.getaclClassByClassName(DemandeComplement.class.getName());
        return demandeComplementService.firstSubmitDemandeComplement(inputDTO, aclClass);
    }

    @PreAuthorize("hasAuthority(@kernelService.demandeComplement_role_canEditDemandeComplement())")
    @PatchMapping("/submitDemandeComplement")
    public DemandeComplementOutputDTO submitDemandeComplement(@RequestBody DemandeComplementInputDTO inputDTO) throws Exception {
        AclClass aclClass = kernelInterface.getaclClassByClassName(DemandeComplement.class.getName());
        return demandeComplementService.submitDemandeComplement(inputDTO, aclClass);
    }
}
