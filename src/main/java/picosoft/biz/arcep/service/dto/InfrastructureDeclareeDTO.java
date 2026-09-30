package picosoft.biz.arcep.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.declaratif.enumeration.TypeInfrastructure;

import java.io.Serializable;

/** Une caracteristique de reseau declaree, rubrique 8.a. */
@Getter
@Setter
@NoArgsConstructor
public class InfrastructureDeclareeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private TypeInfrastructure typeInfrastructure;

    private String precisionDetail;

    private Long demandeDeclaratifId;
}
