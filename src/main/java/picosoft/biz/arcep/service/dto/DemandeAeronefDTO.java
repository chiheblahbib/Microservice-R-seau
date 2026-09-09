package picosoft.biz.arcep.service.dto;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.aeronef.enumeration.*;

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
public class DemandeAeronefDTO implements Serializable {

    private Long id;

    private UUID uuid;

    private Boolean web;

    @Size(max = 25)
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

    @Size(max = 32)
    private String statutDossier;

    /**
     * Teste par la passerelle Gw_signataire : ${data.typeDossier == 'COMMERCE'}.
     * L'omettre enverrait tous les dossiers sur la branche par defaut, sans
     * erreur ni message.
     */
    @Size(max = 50)
    private String typeDossier;

    private NatureDemande natureDemande;

    /** Absente du formulaire papier -- voir DemandeAeronef. */
    @Size(max = 32)
    private String immatriculationAeronef;

    /** Rubrique 4 : null signifie « question non repondue », pas « non ». */
    private Boolean possedaitAeronefRadio;

    /** Rubrique 5 : IFR ou FR. */
    private RestrictionParcours restrictionParcours;

    /** Rubrique 6 : trois cases cumulables, plus la nature du trafic. */
    private Boolean traficVoix;
    private Boolean traficDonnees;
    private Boolean traficAutres;

    @Size(max = 255)
    private String traficAutresPrecision;

    private Boolean traficNational;
    private Boolean traficInternational;

    @Size(max = 25)
    private String referenceAutorisationAnterieure;

    private BigDecimal fraisDossier;

    @Size(max = 8)
    private String deviseFrais;

    @Valid
    private ClientDTO client;

    @Valid
    private ApplicantDTO applicant;

    /** Rubrique 5 : les equipements de bord. */
    @Valid
    private List<EquipementBordAeronefDTO> equipements;

    /**
     * Rubrique 7 : les controles. Remplis par l'ARCEP pendant l'instruction,
     * jamais par le demandeur -- voir DemandeAeronef.
     */
    @Valid
    private List<VerificationControleDTO> verifications;

    /**
     * Le rapport d'instruction. Renseigne par l'ARCEP, jamais par le
     * demandeur -- voir RapportTechnique.
     */
    @Valid
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
    private Long step;
    private String commentaire;
}
