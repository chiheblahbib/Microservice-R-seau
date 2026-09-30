package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.ispc.enumeration.*;

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
public class DemandeIspcOutputDTO implements Serializable {

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

    /**
     * Nature de la demande. Absente du formulaire, conservee pour la
     * plomberie du module -- voir DemandeIspc.
     */
    private NatureDemande natureDemande;

    /**
     * Reference du titre a renouveler.
     *
     * Absente du formulaire, comme `natureDemande` dont elle depend : la
     * fiche ISPC ne prevoit pas le renouvellement. Conservee parce que le
     * module en propose un, et qu'un renouvellement sans reference du titre
     * precedent oblige l'instructeur a le chercher a la main.
     */
    private String referenceAutorisationAnterieure;

    // ------------------------- point semaphore local
    private String fabricantTypeSemaphore;

    private String adressePhysiqueSemaphore;

    /** Demandee au mois pres par le formulaire ; stockee en date complete. */
    private ZonedDateTime dateMiseEnService;

    private String lieuSemaphoreMpt;

    // ------------------------- point semaphore distant
    private String semaphoreDistantNomAdresse;

    private String semaphoreDistantEmplacement;

    /** « S'il est connu » : facultatif, le formulaire le dit. */
    private String semaphoreDistantCodeIspc;

    /** Le code attribue par l'ARCEP : resultat du dossier, vide au depot. */
    private String codeIspcAttribue;

    private BigDecimal fraisDossier;

    private String deviseFrais;

    private ClientDTO client;

    private ApplicantDTO applicant;

    /** Rubrique 5 : les equipements de bord. */
    private List<FonctionPointSemaphoreDTO> fonctions;

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
