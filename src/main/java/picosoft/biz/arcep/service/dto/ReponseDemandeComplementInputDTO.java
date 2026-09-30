package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.client.kernel.model.objects.AttachementInputDTO;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReponseDemandeComplementInputDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String reponse;

    private ZonedDateTime dateReponse;

    private String nomIntervenant;

    private Long demandeComplementId;

    private List<AttachementInputDTO> attachements;
}
