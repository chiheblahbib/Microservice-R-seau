package picosoft.biz.arcep.controller;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import picosoft.biz.arcep.service.SiteImplantationService;
import picosoft.biz.arcep.service.criteria.SiteImplantationCriteria;
import picosoft.biz.arcep.service.dto.SiteImplantationDTO;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SiteImplantationController {

    private final SiteImplantationService siteImplantationService;

    public SiteImplantationController(SiteImplantationService siteImplantationService) {
        this.siteImplantationService = siteImplantationService;
    }

    @PostMapping("/site-implantations")
    public ResponseEntity<SiteImplantationDTO> createSiteImplantation(@RequestBody @Valid SiteImplantationDTO dto) {
        return ResponseEntity.ok(siteImplantationService.save(dto));
    }

    @PutMapping("/site-implantations/{id}")
    public ResponseEntity<SiteImplantationDTO> updateSiteImplantation(@PathVariable Long id, @RequestBody @Valid SiteImplantationDTO dto) {
        return ResponseEntity.ok(siteImplantationService.update(id, dto));
    }

    @GetMapping("/site-implantations")
    public ResponseEntity<Page<SiteImplantationDTO>> getAllSiteImplantations(SiteImplantationCriteria criteria, Pageable pageable,
                                                            @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(siteImplantationService.findAll(criteria, pageable, size));
    }

    @GetMapping("/site-implantations/{id}")
    public ResponseEntity<SiteImplantationDTO> getSiteImplantation(@PathVariable Long id) {
        return siteImplantationService.findOne(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAuthority(@kernelService.station_role_canEditStation())")
    @DeleteMapping("/site-implantations/{id}")
    public ResponseEntity<Void> deleteSiteImplantation(@PathVariable Long id) {
        siteImplantationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
