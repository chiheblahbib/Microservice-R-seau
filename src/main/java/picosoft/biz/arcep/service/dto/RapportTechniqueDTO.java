package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Le rapport technique tel qu'il circule vers le front.
 *
 * Repris de celui d'homologation, avec `dateRapport` EN PLUS : l'original ne le
 * porte pas, alors que l'entite le remplit et que HomologationService le
 * recopie d'un dossier a l'autre. Sans lui, la date existe en base et reste
 * invisible a l'ecran. On ne recopie pas cet oubli.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RapportTechniqueDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /** Necessaire au front : il attache les pieces a (classId, id). */
    private Long classId;

    private String reference;

    private String conclusion;

    private String approvedBy;

    private ZonedDateTime dateRapport;

    private ZonedDateTime sysdateCreated;

    private ZonedDateTime sysdateUpdated;

    private String syscreatedBy;

    private String sysupdatedBy;
}
