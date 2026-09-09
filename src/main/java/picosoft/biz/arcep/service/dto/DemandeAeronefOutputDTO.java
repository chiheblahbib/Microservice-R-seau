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

/** Ce que le service renvoie apres une action de circuit. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DemandeAeronefOutputDTO implements Serializable {

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

    /** Rubrique 2 : le representant, un seul par dossier. */
    @Valid
    private PersonneAeronefDTO representant;

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
