package picosoft.biz.arcep.domain.ussd;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.ussd.enumeration.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Une personne physique du dossier : le requerant, ou le responsable du reseau.
 *
 * POURQUOI PAS LA TABLE PARTAGEE `applicant`
 *
 * Le formulaire exige de chacune une piece d'identite -- CNI, carte de sejour
 * ou passeport -- avec son numero. La table partagee ne porte pas ces colonnes,
 * et les y ajouter toucherait une structure qu'homologation utilise aussi.
 *
 * Le formulaire separe par ailleurs le nom des prenoms, ce que `applicant` ne
 * fait pas non plus : il n'a qu'un `applicantName`.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "personne_ussd", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class PersonneUssd extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ce qui distingue le requerant du responsable du reseau. */

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
     * Un seul representant par dossier : la relation est un-a-un des deux
     * cotes. La cle etrangere reste portee ici, cote enfant.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_ussd_id")
    private DemandeUssd demandeUssd;
}
