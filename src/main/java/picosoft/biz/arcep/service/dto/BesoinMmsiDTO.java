package picosoft.biz.arcep.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.mmsi.enumeration.TypeBesoinMmsi;

import java.io.Serializable;

/** Une case cochee a la rubrique 3 du formulaire MMSI. */
@Getter
@Setter
@NoArgsConstructor
public class BesoinMmsiDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private TypeBesoinMmsi besoin;

    private Long demandeMmsiId;
}
