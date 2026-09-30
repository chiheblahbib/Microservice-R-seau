package picosoft.biz.arcep.domain.ispc;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import picosoft.biz.arcep.domain.ispc.enumeration.FonctionSemaphore;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Une fonction cochee a la rubrique 6.
 *
 * Une table plutot qu'une dizaine de colonnes booleennes : la liste du
 * formulaire est celle des fonctions SS7 connues aujourd'hui, et elle
 * s'allongera. Ajouter une ligne a une enumeration coute moins qu'ajouter une
 * colonne a une table deja peuplee.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fonction_point_semaphore", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class FonctionPointSemaphore implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "fonction")
    private FonctionSemaphore fonction;

    /** N'a de sens que si `fonction` vaut AUTRE : le formulaire dit « preciser ». */
    @Column(name = "precision_autre")
    private String precisionAutre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_ispc_id")
    private DemandeIspc demandeIspc;
}
