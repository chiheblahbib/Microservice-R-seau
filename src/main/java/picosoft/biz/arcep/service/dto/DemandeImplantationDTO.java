package picosoft.biz.arcep.service.dto;

import javax.validation.constraints.Size;
import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.implantation.enumeration.*;

import java.io.Serializable;
import java.util.List;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandeImplantationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private UUID uuid;

    private Boolean web;

    @Size(max = 25)

    private String reference;

    private ZonedDateTime createdDate;

    /**
     * La date de creation en base, distincte de createdDate.
     *
     * Exposee parce que `createdDate` reste vide sur un brouillon : sans elle,
     * la colonne « Date » de l'ecran de suivi serait blanche pour tous les
     * dossiers non encore deposes. En lecture seule -- la valeur vient de
     * l'entite, une valeur envoyee par le client serait ignoree.
     */
    private ZonedDateTime sysdateCreated;

    private ZonedDateTime sendedDate;

    @Size(max = 25)

    private String approvedBy;

    @Size(max = 32)

    private String statutDossier;

    @Size(max = 50)

    private String typeDossier;

    @Valid
    private ClientDTO client;

    @Valid
    private List<ApplicantDTO> applicants;

    @Valid
    /** LA station du dossier : une autorisation en vise une seule. */
    private StationDTO station;

    private String idsPostAttachments;

    private String labelsPostAttachments;

    private String labelsMissingPostAttachments;

    private String wfProcessID;

    private Long classId;

    private String className;

    private String activityName;

    private String assignee;

    private String traitedBy;

    private String sidTraitedBy;

    private Boolean endProcess;

    @Size(max = 64)

    private String state;

    private String signataire;

    private String signataireKcId;

    private Long numberOfattachments;

    private Long step;

    @Size(max = 500)

    private String commentaire;
}
