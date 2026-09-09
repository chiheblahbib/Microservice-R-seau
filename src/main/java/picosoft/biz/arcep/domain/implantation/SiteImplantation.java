package picosoft.biz.arcep.domain.implantation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.implantation.enumeration.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "site_implantation", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class SiteImplantation extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_site", length = 100)
    @Size(max = 100)
    private String nomSite;

    @Column(name = "province", length = 100)
    @Size(max = 100)
    private String province;

    @Column(name = "ville_quartier", length = 100)
    @Size(max = 100)
    private String villeQuartier;

    @Column(name = "departement_canton_village", length = 100)
    @Size(max = 100)
    private String departementCantonVillage;

    // ------------------------ coordonnees ----------------------------
    //
    // Les coordonnees sont conservees en degres / minutes / secondes : c'est
    // sous cette forme qu'elles figurent sur le formulaire papier et sur
    // l'autorisation delivree. Le decimal, lui, se recalcule a la volee.
    //
    // L'HEMISPHERE EST INDISPENSABLE, ce n'est pas un ornement. Le Gabon est a
    // cheval sur l'equateur et sur le meridien : un site a 0 deg 42' Sud aurait
    // pour degres la valeur 0, ou le signe moins ne peut pas se loger. Sans
    // sens explicite, ce site se retrouverait au nord de l'equateur -- une
    // erreur d'environ 150 km, silencieuse, sur une donnee qui sert a verifier
    // les distances de protection.

    /** N ou S. Faute d'indication, on lit le nord. */
    @Column(name = "latitude_sens", length = 1)
    @Size(max = 1)
    private String latitudeSens;

    /** E ou W. Faute d'indication, on lit l'est. */
    @Column(name = "longitude_sens", length = 1)
    @Size(max = 1)
    private String longitudeSens;

    @Column(name = "longitude_degres")
    private Integer longitudeDegres;

    @Column(name = "longitude_minutes")
    private Integer longitudeMinutes;

    @Column(name = "longitude_secondes")
    private Double longitudeSecondes;

    @Column(name = "latitude_degres")
    private Integer latitudeDegres;

    @Column(name = "latitude_minutes")
    private Integer latitudeMinutes;

    @Column(name = "latitude_secondes")
    private Double latitudeSecondes;

    @Column(name = "altitude")
    private Double altitude;

    @Column(name = "description", length = 500)
    @Size(max = 500)
    private String description;

    // ------------- etablissements sensibles a moins de 100 m ---------

    @Column(name = "sensible_scolaire")
    private Boolean sensibleScolaire = false;

    @Column(name = "sensible_creche")
    private Boolean sensibleCreche = false;

    @Column(name = "sensible_sante")
    private Boolean sensibleSante = false;

    @Column(name = "sensible_autres")
    private Boolean sensibleAutres = false;

    @Column(name = "sensible_autres_precision", length = 255)
    @Size(max = 255)
    private String sensibleAutresPrecision;

    // ---------------------- zone protegee ----------------------------

    @Column(name = "zone_classee")
    private Boolean zoneClassee = false;

    @Column(name = "zone_aeroportuaire")
    private Boolean zoneAeroportuaire = false;

    @Column(name = "zone_ferroviaire")
    private Boolean zoneFerroviaire = false;

    @Column(name = "zone_militaire")
    private Boolean zoneMilitaire = false;

    @Column(name = "zone_autres")
    private Boolean zoneAutres = false;

    @Column(name = "zone_autres_precision", length = 255)
    @Size(max = 255)
    private String zoneAutresPrecision;

    // ------------------ pylone a moins de 500 m ----------------------

    @Column(name = "pylone_present")
    private Boolean pylonePresent = false;

    @Column(name = "pylone_raison_sociale", length = 100)
    @Size(max = 100)
    private String pyloneRaisonSociale;

    @Column(name = "pylone_latitude")
    private Double pyloneLatitude;

    @Column(name = "pylone_longitude")
    private Double pyloneLongitude;

    @Column(name = "pylone_hauteur_support")
    private Double pyloneHauteurSupport;

    @Column(name = "pylone_altitude_site")
    private Double pyloneAltitudeSite;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = true, unique = true)
    private Station station;
}
