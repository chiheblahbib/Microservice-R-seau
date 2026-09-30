package picosoft.biz.arcep.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.installateur.enumeration.TypeOutillage;

import java.io.Serializable;

/** Voir l'entite {@link picosoft.biz.arcep.domain.installateur.OutillageDeclare}. */
@Getter
@Setter
@NoArgsConstructor
public class OutillageDeclareDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private TypeOutillage typeOutillage;

    /** La designation saisie, quand typeOutillage vaut AUTRE. */
    private String designation;

    private Long demandeInstallateurId;
}
