package picosoft.biz.arcep.domain.navire;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.navire.enumeration.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Une personne physique du dossier : le demandeur (rubrique 1) ou, s'il en
 * differe, le proprietaire du navire (rubrique 2).
 *
 * PORTE UN ROLE, contrairement a son homologue du dossier d'aeronef. Le
 * formulaire du navire fait declarer deux personnes possibles et ne dit
 * laquelle qu'au titre de sa rubrique ; sans role, rien en base ne
 * distinguerait le demandeur du proprietaire.
 *
 *
 * POURQUOI PAS LA TABLE PARTAGEE `applicant`
 *
 * Le formulaire separe le nom des prenoms, ce que `applicant` ne fait pas : il
 * n'a qu'un `applicantName`. Et il demande une adresse PERMANENTE, propre a la
 * personne, la ou `applicant` ne porte que celle de la structure.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "personne_navire", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class PersonneNavire extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * DEMANDEUR ou PROPRIETAIRE.
     *
     * Chaine et non enumeration, pour s'aligner sur le dossier de reseau : les
     * ecrans filtrent dessus par egalite de chaine, et une enumeration
     * obligerait a la republier des qu'un formulaire ajoute un role.
     */
    @Column(name = "role", length = 32)
    @Size(max = 32)
    private String role;

    @Column(name = "nom", length = 100)
    @Size(max = 100)
    private String nom;

    @Column(name = "prenoms", length = 100)
    @Size(max = 100)
    private String prenoms;

    @Column(name = "fonction", length = 100)
    @Size(max = 100)
    private String fonction;

    @Column(name = "nationalite", length = 50)
    @Size(max = 50)
    private String nationalite;

    /** Le libelle du pays, garde a cote du code comme partout ailleurs. */
    @Column(name = "nationalite_complet", length = 100)
    @Size(max = 100)
    private String nationaliteComplet;



    /** Le formulaire dit « Adresse permanente ». */
    @Column(name = "adresse_permanente", length = 200)
    @Size(max = 200)
    private String adressePermanente;

    @Column(name = "telephone", length = 20)
    @Size(max = 20)
    private String telephone;

    @Column(name = "email", length = 100)
    @Size(max = 100)
    private String email;

    /**
     * Une a deux personnes par dossier : la relation est plusieurs-a-un, la ou
     * l'aeronef se contentait d'un un-a-un. La cle etrangere reste portee ici,
     * cote enfant.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_navire_id")
    private DemandeNavire demandeNavire;
}
