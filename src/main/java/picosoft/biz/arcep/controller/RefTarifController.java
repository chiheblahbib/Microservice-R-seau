package picosoft.biz.arcep.controller;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import picosoft.biz.arcep.service.RefTarifService;
import picosoft.biz.arcep.service.criteria.RefTarifCriteria;
import picosoft.biz.arcep.service.dto.RefTarifDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Bareme tarifaire. Table de reference : elle s'alimente par l'API, sans livraison.
 */
@RestController
@RequestMapping("/api")
public class RefTarifController {

    private final RefTarifService refTarifService;

    public RefTarifController(RefTarifService refTarifService) {
        this.refTarifService = refTarifService;
    }

    @PostMapping("/ref-tarifs")
    public ResponseEntity<RefTarifDTO> create(@RequestBody @Valid RefTarifDTO dto) {
        return ResponseEntity.ok(refTarifService.save(dto));
    }

    @PutMapping("/ref-tarifs/{id}")
    public ResponseEntity<RefTarifDTO> update(@PathVariable Long id, @RequestBody @Valid RefTarifDTO dto) {
        return ResponseEntity.ok(refTarifService.update(id, dto));
    }

    @GetMapping("/ref-tarifs")
    public ResponseEntity<Page<RefTarifDTO>> getAll(RefTarifCriteria criteria, Pageable pageable,
                                                    @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(refTarifService.findAll(criteria, pageable, size));
    }

    @GetMapping("/ref-tarifs/{id}")
    public ResponseEntity<RefTarifDTO> getOne(@PathVariable Long id) {
        return refTarifService.findOne(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /** Le montant applicable aujourd'hui pour un code, eventuellement precise par service. */
    @GetMapping("/ref-tarifs/en-vigueur")
    public ResponseEntity<RefTarifDTO> enVigueur(@RequestParam String code,
                                                 @RequestParam(required = false) String service,
                                                 @RequestParam(required = false)
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                 LocalDate date) {
        return refTarifService.enVigueur(code, service, date)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * Cloture la ligne en vigueur et en ouvre une neuve : une revision tarifaire
     * sans perdre l'historique.
     */
    @PostMapping("/ref-tarifs/{id}/reviser")
    public ResponseEntity<RefTarifDTO> reviser(@PathVariable Long id,
                                               @RequestParam BigDecimal montant,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                               LocalDate dateEffet) {
        return ResponseEntity.ok(refTarifService.reviser(id, montant, dateEffet));
    }

    @DeleteMapping("/ref-tarifs/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        refTarifService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
