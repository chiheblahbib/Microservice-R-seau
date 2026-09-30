package picosoft.biz.arcep.service.dto;

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
import picosoft.biz.arcep.client.kernel.model.objects.AttachementInputDTO;

/** Ce que le front envoie : le dossier, ses pieces et sa decision. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandeDeclaratifInputDTO implements Serializable {

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

    /** Absente du formulaire, conservee pour la plomberie -- voir DemandeDeclaratif. */
    private NatureDemande natureDemande;

    // ---------------- rubrique 4
    private TypeEnregistrement typeEnregistrement;

    private String numeroCertificat;

    /** Le certificat delivre : livrable du dossier, vide au depot. */
    private String certificatDelivre;

    // ---------------- rubrique 1 : ce que `client` ne porte pas
    private String formeJuridique;

    private String boitePostale;

    // ---------------- rubrique 5 : clientele cible, cumulable
    private Boolean clienteleGrandPublic;
    private Boolean clienteleProfessionnels;
    private Boolean clienteleOperateurs;

    // ---------------- rubrique 6
    private TypeCouverture typeCouverture;

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

    private String deviseFrais;

    private ClientDTO client;

    private ApplicantDTO applicant;

    /** Rubrique 5 : les equipements de bord. */
    private List<ServiceDeclareDeclaratifDTO> services;

    /** Rubrique 8.a : les caracteristiques de reseau declarees. */
    private List<InfrastructureDeclareeDTO> infrastructures;

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
    private Long step;
    private String commentaire;

    /** Pieces jointes au depot, publiees ensuite cote kernel. */
    private List<AttachementInputDTO> attachements;

    /** Identifiant du flux choisi dans le circuit, jamais son libelle. */
    private String decision;

    /**
     * Commentaire porte par la transition de circuit.
     *
     * Distinct de `commentaire`, qui est celui du dossier : celui-ci est remis a
     * Flowable et reste attache a l'etape franchie.
     */
    private String WfComment;
}
