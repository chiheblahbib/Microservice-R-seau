package picosoft.biz.arcep.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.ispc.enumeration.FonctionSemaphore;

import javax.validation.constraints.Size;
import java.io.Serializable;

/** Une fonction cochee a la rubrique 6 du formulaire ISPC. */
@Getter
@Setter
@NoArgsConstructor
public class FonctionPointSemaphoreDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private FonctionSemaphore fonction;

    /** N'a de sens que si `fonction` vaut AUTRE. */
    @Size(max = 255)
    private String precisionAutre;

    private Long demandeIspcId;
}
