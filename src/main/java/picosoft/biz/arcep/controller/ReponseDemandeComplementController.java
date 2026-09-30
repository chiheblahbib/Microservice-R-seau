package picosoft.biz.arcep.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import picosoft.biz.arcep.client.kernel.intercomm.KernelInterface;
import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import picosoft.biz.arcep.domain.shared.DemandeComplement;
import picosoft.biz.arcep.domain.shared.ReponseDemandeComplement;
import picosoft.biz.arcep.service.ReponseDemandeComplementService;
import picosoft.biz.arcep.service.dto.ReponseDemandeComplementDTO;
import picosoft.biz.arcep.service.dto.ReponseDemandeComplementInputDTO;
import picosoft.biz.arcep.service.dto.ReponseDemandeComplementResultDTO;

import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ReponseDemandeComplementController {

    private final ReponseDemandeComplementService reponseDemandeComplementService;
    private final KernelInterface kernelInterface;

    public ReponseDemandeComplementController(ReponseDemandeComplementService reponseDemandeComplementService, KernelInterface kernelInterface) {
        this.reponseDemandeComplementService = reponseDemandeComplementService;
        this.kernelInterface = kernelInterface;
    }

    @PostMapping("/reponse-demande-complements")
    public ResponseEntity<ReponseDemandeComplementResultDTO> createReponseDemandeComplement(@RequestBody ReponseDemandeComplementInputDTO inputDTO) throws Exception {
        AclClass aclClass = kernelInterface.getaclClassByClassName(ReponseDemandeComplement.class.getName());
        ReponseDemandeComplementResultDTO result = reponseDemandeComplementService.createReponseWithAttachements(inputDTO, aclClass);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/reponse-demande-complements/{id}")
    public ResponseEntity<ReponseDemandeComplementDTO> updateReponseDemandeComplement(@PathVariable Long id, @RequestBody ReponseDemandeComplementDTO reponseDemandeComplementDTO) {
        ReponseDemandeComplementDTO result = reponseDemandeComplementService.update(id, reponseDemandeComplementDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/reponse-demande-complements")
    public ResponseEntity<Page<ReponseDemandeComplementDTO>> getAllReponseDemandeComplements(Pageable pageable) {
        Page<ReponseDemandeComplementDTO> page = reponseDemandeComplementService.findAll(pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/reponse-demande-complements/{id}")
    public ResponseEntity<ReponseDemandeComplementDTO> getReponseDemandeComplement(@PathVariable Long id) {
        Optional<ReponseDemandeComplementDTO> dto = reponseDemandeComplementService.findOne(id);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/reponse-demande-complements/by-demande-complement/{demandeComplementId}")
    public ResponseEntity<ReponseDemandeComplementDTO> getReponseDemandeComplementByDemandeComplementId(@PathVariable Long demandeComplementId) {
        Optional<ReponseDemandeComplementDTO> dto = reponseDemandeComplementService.findByDemandeComplementId(demandeComplementId);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/reponse-demande-complements/{id}")
    public ResponseEntity<Void> deleteReponseDemandeComplement(@PathVariable Long id) {
        reponseDemandeComplementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
