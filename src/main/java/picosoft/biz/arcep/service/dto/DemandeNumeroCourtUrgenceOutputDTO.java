package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.numerocourturgence.enumeration.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/** Ce que le service renvoie apres une action de circuit. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandeNumeroCourtUrgenceOutputDTO implements Serializable {

    private Long id;

    private UUID uuid;

    private Boolean web;

    private String reference;

    private ZonedDateTime createdDate;
    private ZonedDateTime sendedDate;

    private String approvedBy;

    private String statutDossier;

    /**
     * Teste par la passerelle Gw_signataire : ${data.typeDossier == 'COMMERCE'}.
     * L'omettre enverrait tous les dossiers sur la branche par defaut, sans
     * erreur ni message.
     */
    private String typeDossier;

    private NatureDemande natureDemande;

    // ---------------- rubrique 3
    private NatureActivite natureActivite;

    private String natureActivitePrecision;

    private PorteeReseau porteeReseau;

    private String periodeAttribution;

    // ---------------- rubrique 4
    /** La piece maitresse du dossier -- voir DemandeNumeroCourtUrgence. */
    private String descriptionService;

    private String numeroSouhaite;

    /** Attribue par l'ARCEP, vide au depot. */
    private String numeroAttribue;

    private TypeExploitation typeExploitation;

    private Boolean traficDonnees;
    private Boolean traficVoix;
    private Boolean traficSms;
    private Boolean traficAutres;

    private String traficAutresPrecision;

    private String pointFocalNom;

    private String pointFocalPrenoms;

    private String pointFocalEmail;

    private String pointFocalTelephone;

    private String referenceAutorisationAnterieure;

    private BigDecimal fraisDossier;

    private String deviseFrais;

    private ClientDTO client;

    private ApplicantDTO applicant;

    /** Rubrique 5 : les equipements de bord. */
    private List<NumeroRattachementUrgenceDTO> numerosRattachement;

    /**
     * Le rapport d'instruction. Renseigne par l'ARCEP, jamais par le
     * demandeur -- voir RapportTechnique.
     */
    private RapportTechniqueDTO rapportTechnique;

    /**
     * Les autorisations deja delivrees.
     *
     * INDISPENSABLE au diagramme, et pas seulement informatif : la garde
     * d'idempotence posee sur la passerelle de signature teste
     * ${empty data.attestations}. Sans ce champ sur le DTO serialise dans la
     * variable `data`, l'expression leverait une PropertyNotFoundException a
     * l'execution -- et le circuit se bloquerait au moment de signer.
     */
    private List<AttestationDTO> attestations;

    // ------------------------- workflow / ACL -------------------------

    private String idsPostAttachments;
    private String labelsPostAttachments;
    private String labelsMissingPostAttachments;
    private String signataire;
    private String signataireKcId;
    private String wfProcessID;
    private Long classId;
    private String className;
    private String activityName;
    private String assignee;
    private String traitedBy;
    private String sidTraitedBy;
    private Boolean endProcess;
    private String state;
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
    private String commentaire;
}
