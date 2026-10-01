package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.reseau.enumeration.*;

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
public class DemandeReseauOutputDTO implements Serializable {

    /**
     * FIGE a la valeur que la JVM calculait avant l'ajout d'agentDecision.
     *
     * Flowable range ce DTO, serialise en Java, dans la variable `data` de
     * chaque instance processReseau. Sans valeur explicite, tout champ ajoute
     * change l'identifiant calcule et rend illisibles les dossiers deja en
     * circuit (InvalidClassException a la tache suivante). Au 01/10/2026, 14
     * instances portaient cette valeur ; 4 plus anciennes en portaient une
     * autre et etaient deja illisibles avant ce changement.
     */
    private static final long serialVersionUID = -2665863144270971418L;

    private Long id;

    private UUID uuid;

    private Boolean web;

    private String reference;

    private ZonedDateTime createdDate;
    private ZonedDateTime sendedDate;

    private String approvedBy;

    private String statutDossier;

    private String typeDossier;

    private NatureReseau natureReseau;
    private NatureDemande natureDemande;

    private String referenceAutorisationAnterieure;

    /** Conclusion de l'Etude Technique, imprimee sur le rapport technique. */
    private String conclusion;

    private BigDecimal fraisDossier;

    private String deviseFrais;

    private String engagementNom;

    private String engagementQualite;

    private String engagementLieu;

    private ZonedDateTime engagementDate;

    private ClientDTO client;

    private ApplicantDTO applicant;

    private List<TypeReseauDeclareDTO> typesReseau;

    private List<ServiceDeclareDTO> services;

    private List<SiteReseauDTO> sites;

    private List<LiaisonReseauDTO> liaisons;

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

    /**
     * Nom de l'agent qui prend la decision en cours, pose par le service juste
     * avant de faire avancer le circuit ; jamais persiste.
     *
     * `assignee` ne peut pas tenir ce role : c'est le LIBELLE de l'etape
     * (flowable:assignee, « Service Numerotation »). La decharge, emise a
     * l'« Accepter » de la Numerotation, imprime ce nom comme agent recepteur.
     */
    private String agentDecision;

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
