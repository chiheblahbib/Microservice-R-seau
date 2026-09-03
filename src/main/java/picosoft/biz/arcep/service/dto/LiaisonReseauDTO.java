package picosoft.biz.arcep.service.dto;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.reseau.enumeration.*;

import java.io.Serializable;

/** Une liaison entre deux sites, decrite par ses caracteristiques radio. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LiaisonReseauDTO implements Serializable {

    private Long id;

    private NatureLiaison nature;

    @Size(max = 255)
    private String precisionAutre;

    @Size(max = 100)
    private String bandeFrequences;

    private Double debitEmission;
    private Double debitReception;
    private Integer nombreBonds;

    private Long siteOrigineId;
    private Long siteExtremiteId;

    private Long demandeReseauId;
}
