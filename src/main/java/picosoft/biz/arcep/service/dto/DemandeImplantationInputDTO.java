package picosoft.biz.arcep.service.dto;

import javax.validation.constraints.Size;
import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.client.kernel.model.objects.AttachementInputDTO;
import picosoft.biz.arcep.domain.implantation.enumeration.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Charge utile recue du front pour creer ou faire avancer un dossier.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandeImplantationInputDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private UUID uuid;

    private Boolean web;

    @Size(max = 25)

    private String reference;

    private ZonedDateTime createdDate;

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


    // ---- pilotage du workflow (jamais persiste tel quel) ----

    private List<AttachementInputDTO> attachements;

    private List<AttachementInputDTO> autorisationAttachments;

    /** Id du sequence flow choisi ; voir WorkflowService.getGatewayDecision. */
    private String decision;

    private String WfComment;

    @Size(max = 500)

    private String commentaire;
}
