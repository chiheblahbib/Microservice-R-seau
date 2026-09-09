package picosoft.biz.arcep.domain.navire;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Une autorisation deja detenue par le demandeur, rubrique 4.
 *
 * Le formulaire papier offre DIX lignes numerotees. On ne reproduit pas cette
 * limite : elle vient de la place disponible sur une feuille A4, pas d'une
 * regle. Un armateur qui en detient douze doit pouvoir les declarer.
 *
 * Une entite plutot qu'une chaine a separateurs : ces references servent a
 * retrouver des dossiers anterieurs, et une colonne « ARC-01, ARC-02 » ne se
 * cherche ni ne se joint.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "autorisation_anterieure", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AutorisationAnterieure implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** La reference telle qu'elle figure sur le titre detenu. */
    @Column(name = "reference", length = 100)
    @Size(max = 100)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_navire_id")
    private DemandeNavire demandeNavire;
}
