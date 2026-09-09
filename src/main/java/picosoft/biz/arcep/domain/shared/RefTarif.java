package picosoft.biz.arcep.domain.shared;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.shared.enumeration.MomentTarif;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Barème tarifaire de l'implantation de station.
 *
 * Table de REFERENCE, volontairement vide a la livraison : l'extraction automatique
 * de l'annexe tarifaire est desalignee -- les montants et les lignes "service /
 * application" ne se correspondent plus de facon fiable. La grille doit etre relue
 * a l'oeil sur le PDF avant d'etre saisie.
 *
 * Deux montants sont certains et peuvent etre saisis des maintenant :
 *   FRAIS_ETUDE_DOSSIER      au depot
 *   REDEVANCE_IMPLANTATION   a la delivrance
 * Les frais de controle annuels varient selon le service : c'est cette grille-la
 * qui reste a relire.
 *
 * Le couple (dateEffet, dateFin) permet une revision tarifaire sans redeploiement
 * et sans perdre l'historique : on clot la ligne en cours et on en ouvre une neuve.
 * Un montant deja facture reste donc explicable, meme apres changement de bareme.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ref_tarif", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class RefTarif extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifiant stable de la ligne, ex. FRAIS_ETUDE_DOSSIER. */
    @Column(name = "code", length = 64, nullable = false)
    @Size(max = 64)
    private String code;

    @Column(name = "libelle", length = 255)
    @Size(max = 255)
    private String libelle;

    /**
     * Colonne "Service" de l'annexe. Null pour un tarif forfaitaire qui ne depend
     * pas du service rendu.
     */
    @Column(name = "service", length = 128)
    @Size(max = 128)
    private String service;

    /** Colonne "Application" de l'annexe : la precision qui distingue deux lignes de meme service. */
    @Column(name = "application", length = 255)
    @Size(max = 255)
    private String application;

    @Column(name = "montant", precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(name = "devise", length = 8)
    @Size(max = 8)
    private String devise;

    @Enumerated(EnumType.STRING)
    @Column(name = "moment", length = 16)
    private MomentTarif moment;

    @Column(name = "date_effet")
    private LocalDate dateEffet;

    /** Null tant que la ligne est en vigueur. */
    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "actif")
    private Boolean actif = true;

    @Column(name = "commentaire", length = 500)
    @Size(max = 500)
    private String commentaire;
}
