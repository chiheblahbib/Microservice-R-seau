package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.reseau.enumeration.*;

import java.io.Serializable;

/** Un type de reseau declare. La precision n'a de sens que pour AUTRE. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TypeReseauDeclareDTO implements Serializable {

    private Long id;

    private TypeReseau type;

    private String precisionAutre;

    private Long demandeReseauId;
}
