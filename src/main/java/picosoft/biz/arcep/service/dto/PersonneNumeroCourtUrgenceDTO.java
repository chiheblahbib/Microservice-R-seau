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
 * piece est fournie en piece jointe. Voir PersonneNumeroCourtUrgence.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonneNumeroCourtUrgenceDTO implements Serializable {

    private Long id;

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
    private String telephone;

    @Size(max = 100)
    private String email;

    private Long demandeNumeroCourtUrgenceId;
}
