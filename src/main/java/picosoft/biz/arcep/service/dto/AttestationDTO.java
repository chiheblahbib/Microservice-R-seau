package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttestationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String reference;

    private Long classId;

    private String nomModele;

    private ZonedDateTime sysdateCreated;

    private ZonedDateTime sysdateUpdated;

    private String syscreatedBy;

    private String sysupdatedBy;

}
