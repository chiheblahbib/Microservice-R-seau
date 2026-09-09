package picosoft.biz.arcep.service.dto;

import javax.validation.Valid;
import javax.validation.constraints.Size;
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

    @Size(max = 25)
    private String reference;

    private ZonedDateTime createdDate;
    private ZonedDateTime sendedDate;

    private String approvedBy;

    @Size(max = 32)
    private String statutDossier;

    /**
     * Teste par la passerelle Gw_signataire : ${data.typeDossier == 'COMMERCE'}.
     * L'omettre enverrait tous les dossiers sur la branche par defaut, sans
     * erreur ni message.
     */
    @Size(max = 50)
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
    @Size(max = 25)
    private String referenceAutorisationAnterieure;

    // ------------------------- point semaphore local
    @Size(max = 200)
    private String fabricantTypeSemaphore;

    @Size(max = 255)
    private String adressePhysiqueSemaphore;

    /** Demandee au mois pres par le formulaire ; stockee en date complete. */
    private ZonedDateTime dateMiseEnService;

    private String lieuSemaphoreMpt;

    // ------------------------- point semaphore distant
    @Size(max = 255)
    private String semaphoreDistantNomAdresse;

    @Size(max = 255)
    private String semaphoreDistantEmplacement;

    /** « S'il est connu » : facultatif, le formulaire le dit. */
    @Size(max = 32)
    private String semaphoreDistantCodeIspc;

    /** Le code attribue par l'ARCEP : resultat du dossier, vide au depot. */
    @Size(max = 32)
    private String codeIspcAttribue;

    private BigDecimal fraisDossier;

    @Size(max = 8)
    private String deviseFrais;

    @Valid
    private ClientDTO client;

    @Valid
    private ApplicantDTO applicant;

    /** Rubrique 5 : les equipements de bord. */
    @Valid
    private List<FonctionPointSemaphoreDTO> fonctions;

    /**
     * Le rapport d'instruction. Renseigne par l'ARCEP, jamais par le
     * demandeur -- voir RapportTechnique.
     */
    @Valid
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
    private Long step;
    private String commentaire;
}
