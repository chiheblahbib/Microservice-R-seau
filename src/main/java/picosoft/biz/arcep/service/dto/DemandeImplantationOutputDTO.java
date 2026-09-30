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

    private ApplicantDTO applicant;

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

    /**
     * Ce que le kernel autorise CET utilisateur a faire sur CE dossier :
     * WRITE, READ ou NONE. Pose a la lecture par le service, jamais persiste.
     *
     * Meme champ, meme role et meme nom que sur AsiOutputDTO. Le front s'en
     * sert pour decider si la barre de decisions s'affiche et si les sections
     * sont saisissables : sans lui, l'ecran ne peut qu'ouvrir tout a tout le
     * monde, quelle que soit la tache en cours et quel que soit le role.
     */
    private String userPermission;

    private Long step;

}
