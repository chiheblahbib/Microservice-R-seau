package picosoft.biz.arcep.service.dto;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.domain.numerocourturgence.enumeration.*;

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
public class DemandeNumeroCourtUrgenceInputDTO implements Serializable {

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

    private NatureDemande natureDemande;

    // ---------------- rubrique 3
    private NatureActivite natureActivite;

    @Size(max = 255)
    private String natureActivitePrecision;

    private PorteeReseau porteeReseau;

    @Size(max = 255)
    private String periodeAttribution;

    // ---------------- rubrique 4
    /** La piece maitresse du dossier -- voir DemandeNumeroCourtUrgence. */
    private String descriptionService;

    @Size(max = 16)
    private String numeroSouhaite;

    /** Attribue par l'ARCEP, vide au depot. */
    @Size(max = 16)
    private String numeroAttribue;

    private TypeExploitation typeExploitation;

    private Boolean traficDonnees;
    private Boolean traficVoix;
    private Boolean traficSms;
    private Boolean traficAutres;

    @Size(max = 255)
    private String traficAutresPrecision;

    @Size(max = 100)
    private String pointFocalNom;

    @Size(max = 100)
    private String pointFocalPrenoms;

    @Size(max = 100)
    private String pointFocalEmail;

    @Size(max = 20)
    private String pointFocalTelephone;

    @Size(max = 25)
    private String referenceAutorisationAnterieure;

    private BigDecimal fraisDossier;

    @Size(max = 8)
    private String deviseFrais;

    @Valid
    private ClientDTO client;

    /** Rubrique 2 : le representant, un seul par dossier. */
    @Valid
    private PersonneNumeroCourtUrgenceDTO representant;

    /** Rubrique 5 : les equipements de bord. */
    @Valid
    private List<NumeroRattachementUrgenceDTO> numerosRattachement;

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
