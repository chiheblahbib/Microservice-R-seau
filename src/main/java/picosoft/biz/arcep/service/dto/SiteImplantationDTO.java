package picosoft.biz.arcep.service.dto;

import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.implantation.enumeration.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SiteImplantationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    @Size(max = 100)

    private String nomSite;

    @Size(max = 100)

    private String province;

    @Size(max = 100)

    private String villeQuartier;

    @Size(max = 100)

    private String departementCantonVillage;

    // N/S et E/W : sans eux le signe se perd pour un site proche de l'equateur
    // ou du meridien, ce qui est le cas d'une bonne partie du Gabon.
    @Size(max = 1)
    private String latitudeSens;

    @Size(max = 1)
    private String longitudeSens;

    private Integer longitudeDegres;

    private Integer longitudeMinutes;

    private Double longitudeSecondes;

    private Integer latitudeDegres;

    private Integer latitudeMinutes;

    private Double latitudeSecondes;

    private Double altitude;

    @Size(max = 500)

    private String description;

    private Boolean sensibleScolaire;

    private Boolean sensibleCreche;

    private Boolean sensibleSante;

    private Boolean sensibleAutres;

    @Size(max = 255)

    private String sensibleAutresPrecision;

    private Boolean zoneClassee;

    private Boolean zoneAeroportuaire;

    private Boolean zoneFerroviaire;

    private Boolean zoneMilitaire;

    private Boolean zoneAutres;

    @Size(max = 255)

    private String zoneAutresPrecision;

    private Boolean pylonePresent;

    @Size(max = 100)

    private String pyloneRaisonSociale;

    private Double pyloneLatitude;

    private Double pyloneLongitude;

    private Double pyloneHauteurSupport;

    private Double pyloneAltitudeSite;

    private Long stationId;
}
