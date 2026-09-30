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
public class DemandeReseauDTO implements Serializable {

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

    private String typeDossier;

    private NatureReseau natureReseau;
    private NatureDemande natureDemande;

    private String referenceAutorisationAnterieure;

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
