package picosoft.biz.arcep.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Un code USSD sollicite, rubrique 5. */
@Getter
@Setter
@NoArgsConstructor
public class CodeUssdDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** Chaine : l'etoile et le diese font partie du code. */
    private String code;

    /** Rang de preference : 1 pour le plus souhaite. */
    private Integer rang;

    /** Attribue par l'ARCEP, vide au depot. */
    private String codeAttribue;

    private Long demandeUssdId;
}
