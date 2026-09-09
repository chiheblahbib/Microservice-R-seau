package picosoft.biz.arcep.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.io.Serializable;

/** Un bloc BPQ sollicite ou restitue, rubrique 4. */
@Getter
@Setter
@NoArgsConstructor
public class BlocNumerosDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** Chaine et non entier : « 062 » et « 62 » ne sont pas le meme bloc. */
    @Size(max = 16)
    private String bloc;

    private Integer rang;

    /** Ce que l'ARCEP a attribue, vide au depot. */
    @Size(max = 16)
    private String blocAttribue;

    private Long demandePqId;
}
