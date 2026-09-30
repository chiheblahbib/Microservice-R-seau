package picosoft.biz.arcep.domain.reseau;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.reseau.enumeration.*;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Une liaison entre deux sites du reseau.
 *
 * Les deux extremites pointent des SiteReseau du meme dossier. Elles restent
 * des references d'entite plutot que des noms recopies : renommer un site ne
 * doit pas casser silencieusement la description du reseau.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "liaison_reseau", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class LiaisonReseau extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "nature")
    private NatureLiaison nature;

    @Column(name = "precision_autre")
    private String precisionAutre;

    @Column(name = "bande_frequences")
    private String bandeFrequences;

    /** En Mbit/s, comme le formulaire les demande liaison par liaison. */
    @Column(name = "debit_emission")
    private Double debitEmission;

    @Column(name = "debit_reception")
    private Double debitReception;

    /** Nombre de bonds, exige pour les liaisons FH et BLR. */
    @Column(name = "nombre_bonds")
    private Integer nombreBonds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_origine_id")
    private SiteReseau siteOrigine;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_extremite_id")
    private SiteReseau siteExtremite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_reseau_id")
    private DemandeReseau demandeReseau;
}
