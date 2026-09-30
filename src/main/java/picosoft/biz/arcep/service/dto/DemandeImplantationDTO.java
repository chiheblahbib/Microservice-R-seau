package picosoft.biz.arcep.service.dto;


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
     * Ce que le kernel autorise CET utilisateur a faire sur CE dossier.
     *
     * Meme champ que sur le DTO de sortie, et pour la meme raison : le
     * formulaire lit par ici, et c'est de cette valeur qu'il deduit si ses
     * rubriques sont saisissables. Jamais persiste, jamais relu en entree.
     */
    private String userPermission;

    private Long step;


    private String commentaire;
}
