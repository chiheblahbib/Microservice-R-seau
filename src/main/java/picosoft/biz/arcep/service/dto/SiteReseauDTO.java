package picosoft.biz.arcep.service.dto;

import javax.validation.Valid;
import javax.validation.constraints.Size;
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

    @Size(max = 100)
    private String nomSite;

    @Size(max = 100)
    private String province;

    @Size(max = 100)
    private String ville;

    // N/S et E/W : sans eux le signe se perd pour un site proche de l'equateur
    // ou du meridien, ce qui est le cas d'une bonne partie du Gabon.
    @Size(max = 1)
    private String latitudeSens;

    private Integer latitudeDegres;
    private Integer latitudeMinutes;
    private Double latitudeSecondes;

    @Size(max = 1)
    private String longitudeSens;

    private Integer longitudeDegres;
    private Integer longitudeMinutes;
    private Double longitudeSecondes;

    private Double altitude;

    @Size(max = 500)
    private String description;

    private Long demandeReseauId;
}
