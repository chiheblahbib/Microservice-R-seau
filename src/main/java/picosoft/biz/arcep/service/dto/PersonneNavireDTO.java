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
 * PORTE UN ROLE : le formulaire du navire distingue le demandeur (rubrique 1)
 * du proprietaire (rubrique 2). La piece d'identite, elle, reste une piece
 * jointe et n'a pas de champ ici. Voir PersonneNavire.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonneNavireDTO implements Serializable {

    private Long id;

    @Size(max = 100)
    /** DEMANDEUR ou PROPRIETAIRE. */
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
    private String telephone;

    @Size(max = 100)
    private String email;

    private Long demandeNavireId;
}
