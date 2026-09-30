package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.numerocourt.enumeration.*;

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
public class DemandeNumeroCourtDTO implements Serializable {

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

    // ---------------- rubrique 3
    private NatureActivite natureActivite;

    private String natureActivitePrecision;

    private PorteeReseau porteeReseau;

    /** Texte libre : duree, echeance ou dates, le formulaire ne tranche pas. */
    private String periodeAttribution;

    // ---------------- rubrique 4
    /** Classique (8XYZ) ou gold (8X8X). Commande le tarif. */
    private TypeNumeroCourt typeNumero;

    /** Absent des rubriques ; facultatif -- voir DemandeNumeroCourt. */
    private String numeroSouhaite;

    /** Attribue par l'ARCEP, vide au depot. */
    private String numeroAttribue;

    private TypeExploitation typeExploitation;

    private ModeExploitation modeExploitation;

    /** N'a de sens que si modeExploitation vaut NORMAL. */
    private TypeFacturation typeFacturation;

    private Boolean traficDonnees;
    private Boolean traficVoix;
    private Boolean traficSms;
    private Boolean traficAutres;

    private String traficAutresPrecision;

    /** Le point focal : quatre champs, a plat -- voir DemandeNumeroCourt. */
    private String pointFocalNom;

    private String pointFocalPrenoms;

    private String pointFocalEmail;

    private String pointFocalTelephone;

    private String referenceAutorisationAnterieure;

    /** Le seul montant du formulaire ; fraisDossier reste vide. */
    private BigDecimal redevanceAnnuelle;

    private BigDecimal fraisDossier;

    private String deviseFrais;

    private ClientDTO client;

    private ApplicantDTO applicant;

    /** Rubrique 5 : les equipements de bord. */
    private List<NumeroRattachementCourtDTO> numerosRattachement;

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
