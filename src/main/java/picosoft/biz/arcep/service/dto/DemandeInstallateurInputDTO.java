package picosoft.biz.arcep.service.dto;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.installateur.enumeration.*;

import picosoft.biz.arcep.domain.installateur.enumeration.EtendueActivite;
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
public class DemandeInstallateurInputDTO implements Serializable {

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

    /** Nouvelle demande, ou renouvellement -- voir DemandeInstallateur. */
    private NatureDemande natureDemande;

    @Size(max = 25)
    private String referenceAutorisationAnterieure;

    // ---------------- identite de l'entreprise : ce que `client` ne porte pas
    @Size(max = 100)
    private String formeJuridique;

    @Size(max = 50)
    private String nif;

    @Size(max = 50)
    private String boitePostale;

    /** Distincte de l'adresse postale : sert a trouver les locaux. */
    @Size(max = 255)
    private String localisation;

    // ---------------- rubrique 2
    @Size(max = 255)
    private String etendueAutrePrecision;

    // ---------------- rubrique 3 : deux questions oui / non / non repondu
    private Boolean permanenceAccueil;
    private Boolean repondeurEnregistreur;

    // ---------------- rubrique 6
    /** En metres carres : une vraie mesure, d'ou un decimal. */
    private BigDecimal surfaceLocaux;

    // ---------------- rubrique 7-1
    private String vehicules;

    // ---------------- rubrique 8
    private Boolean zoneLibreville;

    @Size(max = 255)
    private String autresLocalites;

    private String agences;

    // ---------------- rubrique 9
    private String activitesAnterieures;

    // ---------------- rubrique 11
    /** Communiquee apres validation ; enregistree au depot. */
    private BigDecimal redevanceAnnuelle;

    private BigDecimal fraisDossier;

    @Size(max = 8)
    private String deviseFrais;

    @Valid
    private ClientDTO client;

    /** Rubrique 2 : le representant, un seul par dossier. */
    @Valid
    private List<PersonneInstallateurDTO> personnes;

    /** Rubrique 5 : les equipements de bord. */
    @Valid
    private List<QualiteDemandeeDTO> qualites;

    /** Rubrique 2 : l'etendue de l'activite, cumulable. */
    private java.util.Set<EtendueActivite> etendues;

    /** Rubrique 5 : les techniciens specialistes. */
    private List<TechnicienSpecialisteDTO> techniciens;

    /** Rubrique 7-2 : l'outillage professionnel. */
    private List<OutillageDeclareDTO> outillages;

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
