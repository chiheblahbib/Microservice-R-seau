package picosoft.biz.arcep.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.declaratif.enumeration.TypeServiceDeclare;

import java.io.Serializable;

/** Un service declare et son calendrier, rubrique 7. */
@Getter
@Setter
@NoArgsConstructor
public class ServiceDeclareDeclaratifDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private TypeServiceDeclare typeService;

    /** N'a de sens que si typeService vaut AUTRE. */
    private String precisionAutre;

    /** Texte libre : « T3 2026 » comme une date precise y sont recevables. */
    private String calendrier;

    private Long demandeDeclaratifId;
}
