package picosoft.biz.arcep.service.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Voir l'entite {@link picosoft.biz.arcep.domain.installateur.TechnicienSpecialiste}. */
@Getter
@Setter
@NoArgsConstructor
public class TechnicienSpecialisteDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String nom;

    private String qualification;

    /** Texte libre : « 12 ans chez Gabon Telecom » y est recevable. */
    private String experience;

    private String fonction;

    private Long demandeInstallateurId;
}
