package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.navire.enumeration.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Le dossier reseau, tel qu'il circule entre le front et le service.
 *
 * Les bornes de taille reprennent celles des colonnes : le back les fait
 * respecter lui-meme, ce qui produit un 400 explicite plutot qu'une erreur 500
 * de la base.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandeNavireDTO implements Serializable {

    private Long id;

    private UUID uuid;

    private Boolean web;

    private String reference;

    /**
     * Date de DEPOT : posee par initAndSubmit, donc nulle tant que le dossier
     * est un brouillon.
     */
    private ZonedDateTime createdDate;
    private ZonedDateTime sendedDate;

    /**
     * Date de CREATION de la ligne, ecrite par l'audit Hibernate des le premier
     * enregistrement.
     *
     * Exposee parce que `createdDate` reste vide sur un brouillon : sans elle,
     * la colonne « Date » de l'ecran de suivi serait blanche pour tous les
     * dossiers non encore deposes. En lecture seule -- la valeur vient de
     * l'entite, une valeur envoyee par le client serait ignoree (updatable=false).
     */
    private ZonedDateTime sysdateCreated;

    private String approvedBy;

    private String statutDossier;

    /**
     * Teste par la passerelle Gw_signataire : ${data.typeDossier == 'COMMERCE'}.
     * L'omettre enverrait tous les dossiers sur la branche par defaut, sans
     * erreur ni message.
     */
    private String typeDossier;

    private NatureDemande natureDemande;

    /** Rubrique 3 : le nom du navire. */
    private String nomNavire;

    /** Absente du formulaire papier -- voir DemandeNavire. */
    private String immatriculationNavire;

    /** Rubrique 4 : null signifie « question non repondue », pas « non ». */
    private Boolean possedaitNavireRadio;

    /** Rubrique 4 : le numero de l'autorisation deja detenue. */
    private String numeroAutorisation;

    /** Rubrique 5.1 : ou la station est implantee. */
    private String province;

    private String localite;

    private String quartier;

    /** Degres decimaux ; BigDecimal pour que la saisie soit rendue exacte. */
    private BigDecimal latitude;

    private BigDecimal longitude;

    /** Creation, extension, modification, autre. */
    private MotifImplantation motifImplantation;

    private String motifImplantationPrecision;

    /** Rubrique 5.2 : satellite, hertzienne, autre. */
    private TypeStation typeStation;

    private String typeStationPrecision;

    private String bandeFrequencesExploitee;

    /** Rubrique 6 : trois cases cumulables, plus la nature du trafic. */
    private Boolean traficVoix;
    private Boolean traficDonnees;
    private Boolean traficAutres;

    private String traficAutresPrecision;

    private Boolean traficNational;
    private Boolean traficInternational;

    private String referenceAutorisationAnterieure;

    private BigDecimal fraisDossier;

    /** Due apres jugement de conformite, quand les frais accompagnent le depot. */
    private BigDecimal redevanceAnnuelle;

    private String deviseFrais;

    private ClientDTO client;

    private ApplicantDTO applicant;

    /** Rubrique 4 : les autorisations deja obtenues, une par ligne. */
    private List<AutorisationAnterieureDTO> autorisationsAnterieures;

    /** Rubrique 5 : les equipements de bord. */
    private List<EquipementBordNavireDTO> equipements;

    /**
     * Le rapport d'instruction. Renseigne par l'ARCEP, jamais par le
     * demandeur -- voir RapportTechnique.
     */
    private RapportTechniqueDTO rapportTechnique;

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
