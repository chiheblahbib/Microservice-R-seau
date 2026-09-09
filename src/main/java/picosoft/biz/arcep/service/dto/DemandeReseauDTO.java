package picosoft.biz.arcep.service.dto;

import javax.validation.Valid;
import javax.validation.constraints.Size;
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

    @Size(max = 50)
    private String typeDossier;

    private NatureReseau natureReseau;
    private NatureDemande natureDemande;

    @Size(max = 25)
    private String referenceAutorisationAnterieure;

    private BigDecimal fraisDossier;

    @Size(max = 8)
    private String deviseFrais;

    @Size(max = 100)
    private String engagementNom;

    @Size(max = 100)
    private String engagementQualite;

    @Size(max = 100)
    private String engagementLieu;

    private ZonedDateTime engagementDate;

    @Valid
    private ClientDTO client;

    @Valid
    private ApplicantDTO applicant;

    @Valid
    private List<TypeReseauDeclareDTO> typesReseau;

    @Valid
    private List<ServiceDeclareDTO> services;

    @Valid
    private List<SiteReseauDTO> sites;

    @Valid
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
    private Long step;
    private String commentaire;
}
