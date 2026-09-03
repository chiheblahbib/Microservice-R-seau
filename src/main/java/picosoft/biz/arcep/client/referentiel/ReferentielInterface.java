package picosoft.biz.arcep.client.referentiel;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import picosoft.biz.arcep.client.config.FeignConfig;
import picosoft.biz.arcep.client.referentiel.model.ContactDTO;

import java.util.List;


@FeignClient(value = "${feign.referentiel.name}", url = "${feign.referentiel.url}", configuration = FeignConfig.class)
public interface ReferentielInterface {

    @GetMapping("/contacts/userId/{userId}")
    public ResponseEntity<ContactDTO> getByUserId(@PathVariable String userId);

    @GetMapping("/contacts/email/{email}")
    public ResponseEntity<ContactDTO> getByEmail(@PathVariable String email);
}
