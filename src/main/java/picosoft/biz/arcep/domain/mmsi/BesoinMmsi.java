package picosoft.biz.arcep.domain.mmsi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import picosoft.biz.arcep.domain.mmsi.enumeration.TypeBesoinMmsi;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Une case cochee a la rubrique 3.
 *
 * Une table plutot que seize colonnes booleennes : le formulaire en propose
 * seize aujourd'hui, reparties en quatre blocs, et l'UIT en ajoute
 * regulierement. Ajouter une valeur a une enumeration coute moins qu'ajouter
 * une colonne a une table deja peuplee.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "besoin_mmsi", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BesoinMmsi implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "besoin", length = 48)
    private TypeBesoinMmsi besoin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_mmsi_id")
    private DemandeMmsi demandeMmsi;
}
