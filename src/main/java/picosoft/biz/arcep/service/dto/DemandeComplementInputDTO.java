package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.client.kernel.model.objects.AttachementInputDTO;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandeComplementInputDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

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

    private List<AttachementInputDTO> attachements;

    private String decision;

    private String wfComment;

    private Long classIdDossier;

    private Long objectIdDossier;

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
