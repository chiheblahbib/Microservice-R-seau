package picosoft.biz.arcep.service.dto;

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
 * Etat renvoye au front, et objet transporte dans la variable BPMN 'data'.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandeImplantationOutputDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private UUID uuid;

    private Boolean web;

    private String reference;

    private ZonedDateTime createdDate;

    private ZonedDateTime sendedDate;

    private String approvedBy;

    private String statutDossier;

    private String typeDossier;

    private ClientDTO client;

    private List<ApplicantDTO> applicants;

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

    private String state;

    private String signataire;

    private String signataireKcId;

    private Long numberOfattachments;

    private Long step;

}
