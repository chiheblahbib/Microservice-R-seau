package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.implantation.enumeration.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FrequenceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private SensFrequence sens;

    private Double frequenceCentraleMhz;

    private Double bandeMhz;

    private Double zoneServiceKm;

    private Double parWatts;

    private Long stationId;
}
