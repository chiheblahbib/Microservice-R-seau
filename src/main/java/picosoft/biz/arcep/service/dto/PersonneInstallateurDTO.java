package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Le representant de l'operateur, rubrique 2 du formulaire.
 *
 * Ni role ni piece d'identite : le formulaire n'en demande qu'un seul, et la
 * piece est fournie en piece jointe. Voir PersonneInstallateur.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonneInstallateurDTO implements Serializable {

    private Long id;

    @Size(max = 100)
    /** REPRESENTANT, REQUERANT ou RESPONSABLE. */
    @Size(max = 32)
    private String role;

    @Size(max = 100)
    private String nom;

    @Size(max = 100)
    private String prenoms;

    @Size(max = 100)
    private String fonction;

    @Size(max = 50)
    private String nationalite;

    @Size(max = 100)
    private String nationaliteComplet;

    /** Le formulaire dit « Adresse permanente ». */
    @Size(max = 200)
    private String adressePermanente;

    @Size(max = 20)
    /** Demandees du seul responsable de l'activite, rubrique 4. */
    @Size(max = 200)
    private String qualification;

    @Size(max = 255)
    private String experience;

    @Size(max = 20)
    private String telephone;

    @Size(max = 100)
    private String email;

    private Long demandeInstallateurId;
}
