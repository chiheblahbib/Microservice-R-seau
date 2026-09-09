package picosoft.biz.arcep.service.dto;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.declaratif.enumeration.*;

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
public class DemandeDeclaratifDTO implements Serializable {

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

    /** Absente du formulaire, conservee pour la plomberie -- voir DemandeDeclaratif. */
    private NatureDemande natureDemande;

    // ---------------- rubrique 4
    private TypeEnregistrement typeEnregistrement;

    @Size(max = 50)
    private String numeroCertificat;

    /** Le certificat delivre : livrable du dossier, vide au depot. */
    @Size(max = 50)
    private String certificatDelivre;

    // ---------------- rubrique 1 : ce que `client` ne porte pas
    @Size(max = 100)
    private String formeJuridique;

    @Size(max = 50)
    private String boitePostale;

    // ---------------- rubrique 5 : clientele cible, cumulable
    private Boolean clienteleGrandPublic;
    private Boolean clienteleProfessionnels;
    private Boolean clienteleOperateurs;

    // ---------------- rubrique 6
    private TypeCouverture typeCouverture;

    @Size(max = 255)
    private String provinces;

    // ---------------- rubrique 8 : quatre questions oui / non / non repondu
    private Boolean reseauCollecteHertzien;
    private Boolean reseauCollecteFilaire;
    private String reseauCollecteDescription;

    private Boolean coeurReseauPropre;
    private String coeurReseauDescription;

    private Boolean plateformesPropres;
    private String plateformesDescription;

    private Boolean produitsDeGros;
    private String produitsDeGrosDescription;

    // ---------------- rubrique 9
    /** Le tarif aux USAGERS : une grille, pas une somme due a l'ARCEP. */
    private String tarifUsagers;

    private BigDecimal fraisDossier;

    @Size(max = 8)
    private String deviseFrais;

    @Valid
    private ClientDTO client;

    @Valid
    private ApplicantDTO applicant;

    /** Rubrique 5 : les equipements de bord. */
    @Valid
    private List<ServiceDeclareDeclaratifDTO> services;

    /** Rubrique 8.a : les caracteristiques de reseau declarees. */
    private List<InfrastructureDeclareeDTO> infrastructures;

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
