package picosoft.biz.arcep.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.io.Serializable;

/** Voir l'entite {@link picosoft.biz.arcep.domain.installateur.TechnicienSpecialiste}. */
@Getter
@Setter
@NoArgsConstructor
public class TechnicienSpecialisteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @Size(max = 150)
    private String nom;

    @Size(max = 200)
    private String qualification;

    /** Texte libre : « 12 ans chez Gabon Telecom » y est recevable. */
    @Size(max = 255)
    private String experience;

    @Size(max = 150)
    private String fonction;

    private Long demandeInstallateurId;
}
