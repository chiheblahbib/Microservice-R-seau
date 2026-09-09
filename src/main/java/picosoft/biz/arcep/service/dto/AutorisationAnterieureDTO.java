package picosoft.biz.arcep.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.io.Serializable;

/** Une autorisation deja detenue, rubrique 4 du formulaire. */
@Getter
@Setter
@NoArgsConstructor
public class AutorisationAnterieureDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @Size(max = 100)
    private String reference;

    private Long demandeNavireId;
}
