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
import picosoft.biz.arcep.client.kernel.model.objects.AttachementInputDTO;

/** Ce que le front envoie : le dossier, ses pieces et sa decision. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandeNumeroCourtInputDTO implements Serializable {

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
