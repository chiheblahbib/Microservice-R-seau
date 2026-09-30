package picosoft.biz.arcep.domain.installateur;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Un technicien specialiste de la profession, rubrique 5.
 *
 * Le formulaire demande « Nom - Qualification - Experience Professionnelle -
 * Fonction (joindre les CV) » : quatre colonnes, autant de fois qu'il y a de
 * techniciens. C'est ce qui fonde la competence technique de l'entreprise, et
 * l'instruction juge dessus.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "technicien_specialiste", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TechnicienSpecialiste implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom")
    private String nom;

    @Column(name = "qualification")
    private String qualification;

    /**
     * L'experience professionnelle, en texte libre.
     *
     * Le formulaire n'ouvre qu'une colonne et ne demande pas un nombre
     * d'annees : « 12 ans chez Gabon Telecom » y est aussi recevable que
     * « 12 ». Un entier aurait refuse la premiere reponse.
     */
    @Column(name = "experience")
    private String experience;

    @Column(name = "fonction")
    private String fonction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_installateur_id")
    private DemandeInstallateur demandeInstallateur;
}
