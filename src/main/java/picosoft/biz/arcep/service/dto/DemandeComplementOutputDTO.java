package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandeComplementOutputDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private UUID uuid;

    private String reference;

    private String referenceDossier;

    private String subject;

    private String description;

    private ZonedDateTime createdDate;

    private ZonedDateTime sendedDate;

    private String approvedBy;

    private String categorie;

    private String idsPostAttachments;

    private String labelsPostAttachments;

    private String labelsMissingPostAttachments;

    private String wfProcessID;

    private Long classId;

    private Long classIdDossier;

    private Long objectIdDossier;

    private String className;

    private String activityName;

    private String assignee;

    private Boolean endProcess;

    private String state;

    private String stateDemande;

    private String userPermission;

    private Long step;

    private Long numberOfattachments;

    private String affectedSid;

    private String affectedName;

    private String affectedKeycloakId;

    private String sidExterne;

    private Boolean delaiReponse;

    private Boolean web;

    private Boolean validation;

    private String commentaire;

    private String nomIntervenant;

    private String typeDossier;

    private Long asiId;


    private ReponseDemandeComplementDTO reponseDemandeComplement;

    private DestinataireDTO destinataire;
}
