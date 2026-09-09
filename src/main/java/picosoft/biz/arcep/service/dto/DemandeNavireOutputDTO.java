package picosoft.biz.arcep.service.dto;

import javax.validation.Valid;
import javax.validation.constraints.Size;
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

/** Ce que le service renvoie apres une action de circuit. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandeNavireOutputDTO implements Serializable {

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

    /** Rubrique 3 : le nom du navire. */
    @Size(max = 100)
    private String nomNavire;

    /** Absente du formulaire papier -- voir DemandeNavire. */
    @Size(max = 32)
    private String immatriculationNavire;

    /** Rubrique 4 : null signifie « question non repondue », pas « non ». */
    private Boolean possedaitNavireRadio;

    /** Rubrique 4 : le numero de l'autorisation deja detenue. */
    @Size(max = 50)
    private String numeroAutorisation;

    /** Rubrique 5.1 : ou la station est implantee. */
    @Size(max = 100)
    private String province;

    @Size(max = 100)
    private String localite;

    @Size(max = 100)
    private String quartier;

    /** Degres decimaux ; BigDecimal pour que la saisie soit rendue exacte. */
    private BigDecimal latitude;

    private BigDecimal longitude;

    /** Creation, extension, modification, autre. */
    private MotifImplantation motifImplantation;

    @Size(max = 255)
    private String motifImplantationPrecision;

    /** Rubrique 5.2 : satellite, hertzienne, autre. */
    private TypeStation typeStation;

    @Size(max = 255)
    private String typeStationPrecision;

    @Size(max = 150)
    private String bandeFrequencesExploitee;

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

    /** Due apres jugement de conformite, quand les frais accompagnent le depot. */
    private BigDecimal redevanceAnnuelle;

    @Size(max = 8)
    private String deviseFrais;

    @Valid
    private ClientDTO client;

    @Valid
    private ApplicantDTO applicant;

    /** Rubrique 4 : les autorisations deja obtenues, une par ligne. */
    @Valid
    private List<AutorisationAnterieureDTO> autorisationsAnterieures;

    /** Rubrique 5 : les equipements de bord. */
    @Valid
    private List<EquipementBordNavireDTO> equipements;

    /**
     * Le rapport d'instruction. Renseigne par l'ARCEP, jamais par le
     * demandeur -- voir RapportTechnique.
     */
    @Valid
    private RapportTechniqueDTO rapportTechnique;

    /**
     * Les autorisations deja delivrees.
     *
     * INDISPENSABLE au diagramme, et pas seulement informatif : la garde
     * d'idempotence posee sur la passerelle de signature teste
     * ${empty data.attestations}. Sans ce champ sur le DTO serialise dans la
     * variable `data`, l'expression leverait une PropertyNotFoundException a
     * l'execution -- et le circuit se bloquerait au moment de signer.
     */
    private List<AttestationDTO> attestations;

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
