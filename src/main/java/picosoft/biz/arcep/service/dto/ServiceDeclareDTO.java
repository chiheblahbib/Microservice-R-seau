package picosoft.biz.arcep.service.dto;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.reseau.enumeration.*;

import java.io.Serializable;

/** Un service declare, avec sa portee geographique s'il en a une. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDeclareDTO implements Serializable {

    private Long id;

    private TypeService type;

    /** Nulle hors telephonie : le formulaire ne decline que celle-ci. */
    private PorteeService portee;

    private Long demandeReseauId;
}
