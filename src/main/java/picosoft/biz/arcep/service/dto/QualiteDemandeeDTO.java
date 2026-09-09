package picosoft.biz.arcep.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.installateur.enumeration.TypeAutorisation;

import javax.validation.constraints.Size;
import java.io.Serializable;

/** Voir l'entite {@link picosoft.biz.arcep.domain.installateur.QualiteDemandee}. */
@Getter
@Setter
@NoArgsConstructor
public class QualiteDemandeeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private TypeAutorisation qualite;

    private Long demandeInstallateurId;
}
