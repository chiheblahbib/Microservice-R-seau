package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.reseau.enumeration.*;

import java.io.Serializable;

/** Un site du reseau, coordonnees comprises. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SiteReseauDTO implements Serializable {

    private Long id;

    private String nomSite;

    private String province;

    private String ville;

    // N/S et E/W : sans eux le signe se perd pour un site proche de l'equateur
    // ou du meridien, ce qui est le cas d'une bonne partie du Gabon.
    private String latitudeSens;

    private Integer latitudeDegres;
    private Integer latitudeMinutes;
    private Double latitudeSecondes;

    private String longitudeSens;

    private Integer longitudeDegres;
    private Integer longitudeMinutes;
    private Double longitudeSecondes;

    private Double altitude;

    private String description;

    private Long demandeReseauId;
}
