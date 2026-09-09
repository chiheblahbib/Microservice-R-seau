package picosoft.biz.arcep.controller;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import picosoft.biz.arcep.service.FrequenceService;
import picosoft.biz.arcep.service.criteria.FrequenceCriteria;
import picosoft.biz.arcep.service.dto.FrequenceDTO;

import java.util.List;

@RestController
@RequestMapping("/api")
public class FrequenceController {

    private final FrequenceService frequenceService;

    public FrequenceController(FrequenceService frequenceService) {
        this.frequenceService = frequenceService;
    }

    @PostMapping("/frequences")
    public ResponseEntity<FrequenceDTO> createFrequence(@RequestBody @Valid FrequenceDTO dto) {
        return ResponseEntity.ok(frequenceService.save(dto));
    }

    @PutMapping("/frequences/{id}")
    public ResponseEntity<FrequenceDTO> updateFrequence(@PathVariable Long id, @RequestBody @Valid FrequenceDTO dto) {
        return ResponseEntity.ok(frequenceService.update(id, dto));
    }

    @GetMapping("/frequences")
    public ResponseEntity<Page<FrequenceDTO>> getAllFrequences(FrequenceCriteria criteria, Pageable pageable,
                                                            @RequestParam(value = "size", required = false) Integer size) {
        return ResponseEntity.ok(frequenceService.findAll(criteria, pageable, size));
    }

    @GetMapping("/frequences/{id}")
    public ResponseEntity<FrequenceDTO> getFrequence(@PathVariable Long id) {
        return frequenceService.findOne(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stations/{stationId}/frequences")
    public ResponseEntity<List<FrequenceDTO>> getByStation(@PathVariable Long stationId) {
        return ResponseEntity.ok(frequenceService.findByStation(stationId));
    }

    @DeleteMapping("/frequences/{id}")
    public ResponseEntity<Void> deleteFrequence(@PathVariable Long id) {
        frequenceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
