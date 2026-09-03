package picosoft.biz.arcep.service.dto;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.reseau.enumeration.*;

import java.io.Serializable;

/** Requerant ou responsable du reseau, distingues par leur role. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PersonneReseauDTO implements Serializable {

    private Long id;

    private RolePersonne role;

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

    private TypePieceIdentite typePieceIdentite;

    @Size(max = 50)
    private String numeroPieceIdentite;

    @Size(max = 200)
    private String adresse;

    @Size(max = 20)
    private String telephone;

    @Size(max = 100)
    private String email;

    private Long demandeReseauId;
}
