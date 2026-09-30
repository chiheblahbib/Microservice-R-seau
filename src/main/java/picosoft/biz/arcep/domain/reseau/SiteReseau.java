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
 * Un site du reseau : ou le materiel est implante.
 *
 * Les coordonnees reprennent le decoupage D/M/S de la demande d'implantation,
 * HEMISPHERE COMPRIS. Ce n'est pas une coquetterie : le Gabon est a cheval sur
 * l'equateur et sur le meridien, et un site a 0 deg 42' Sud a pour degres la
 * valeur 0, ou le signe moins ne peut pas se loger.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "site_reseau", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class SiteReseau extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_site")
    private String nomSite;

    @Column(name = "province")
    private String province;

    @Column(name = "ville")
    private String ville;

    /** N ou S. Faute d'indication, on lit le nord. */
    @Column(name = "latitude_sens")
    private String latitudeSens;

    @Column(name = "latitude_degres")
    private Integer latitudeDegres;

    @Column(name = "latitude_minutes")
    private Integer latitudeMinutes;

    @Column(name = "latitude_secondes")
    private Double latitudeSecondes;

    /** E ou W. Faute d'indication, on lit l'est. */
    @Column(name = "longitude_sens")
    private String longitudeSens;

    @Column(name = "longitude_degres")
    private Integer longitudeDegres;

    @Column(name = "longitude_minutes")
    private Integer longitudeMinutes;

    @Column(name = "longitude_secondes")
    private Double longitudeSecondes;

    @Column(name = "altitude")
    private Double altitude;

    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_reseau_id")
    private DemandeReseau demandeReseau;
}
