package picosoft.biz.arcep.domain.numerocourturgence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.shared.Applicant;
import picosoft.biz.arcep.domain.shared.RapportTechnique;
import picosoft.biz.arcep.domain.numerocourturgence.enumeration.*;
import picosoft.biz.arcep.client.kernel.model.acl.AclObjectIdentity;
import picosoft.biz.arcep.domain.shared.Attestation;
import picosoft.biz.arcep.domain.shared.Client;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Demande d'attribution d'un numero court de service d'urgence.
 *
 * Reprend le formulaire de l'ARCEP : operateur (1), representant (2), nature
 * de l'activite (3), specification de services (4), pieces (5),
 * engagement (6).
 *
 *
 * LE MEME FORMULAIRE QUE LE NUMERO COURT, AMPUTE DE TROIS CHOSES
 *
 *   - PAS DE TYPE DE NUMERO. Ni classique ni gold : un numero d'urgence n'est
 *     pas un produit qu'on choisit.
 *   - PAS DE MODE NI DE FACTURATION. Le formulaire tranche lui-meme :
 *     « appels gratuits pour les appelants, aucune facturation a la charge du
 *     titulaire du numero ». Personne ne paie, la question ne se pose pas.
 *   - PAS D'ANNEXE TARIFAIRE. Aucun montant, nulle part.
 *
 * Et une chose EN PLUS : la description du service d'urgence, rubrique 4, a
 * qui le formulaire consacre pres de deux pages de lignes.
 *
 *
 * UN SERVICE RESERVE
 *
 * Le formulaire rappelle en tete que l'attribution est « reservee uniquement
 * aux services d'urgence (SAMU, POMPIER, etc.) et de securite (GENDARMERIE,
 * POLICE, etc.) ». Cette restriction n'est PAS modelisee : c'est une regle
 * d'instruction, que l'agent applique en lisant le document administratif
 * portant creation de l'entite -- piece exigee a la rubrique 5. La coder en
 * liste fermee d'organismes aurait refuse le prochain service cree.
 *
 *
 * L'ENGAGEMENT SUR L'HONNEUR N'EST PAS SAISI
 *
 * La rubrique figure bien sur l'imprime papier, mais le dossier ne la reprend
 * pas : le circuit trace deja qui a depose et qui a signe -- `approvedBy`, le
 * signataire, la date de depot, puis le rapport technique et l'attestation.
 * Redemander au demandeur de se nommer une fois de plus n'ajoutait aucune
 * information, et donnait quatre champs de plus a remplir.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "demande_numerocourturgence", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class DemandeNumeroCourtUrgence extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID uuid;

    @Column(name = "web")
    private Boolean web;

    @Column(name = "reference", length = 25)
    @Size(max = 25)
    private String reference;

    @Column(name = "created_date", length = 25)
    private ZonedDateTime createdDate;

    @Column(name = "sended_date", length = 25)
    private ZonedDateTime sendedDate;

    @Column(name = "approvedBy")
    private String approvedBy;

    /**
     * Compare tel quel par les passerelles du circuit : ${data.statutDossier == 'soumis'}.
     * Chaine et non enumeration, pour s'aligner sur Asi et sur les diagrammes
     * deployes, qui testent des chaines nues. La valeur est posee par le front.
     */
    @Column(name = "statut_dossier", length = 32)
    @Size(max = 32)
    private String statutDossier;

    @Column(name = "type_dossier", length = 50)
    @Size(max = 50)
    private String typeDossier;

    // ---------------------------- rubrique 3 ----------------------------

    /** Nouvelle demande, modification, renouvellement. */
    @Enumerated(EnumType.STRING)
    @Column(name = "nature_demande", length = 16)
    private NatureDemande natureDemande;

    /** Mobile, fixe, ou autre reseau / service. */
    @Enumerated(EnumType.STRING)
    @Column(name = "nature_activite", length = 24)
    private NatureActivite natureActivite;

    /** N'a de sens que si natureActivite vaut AUTRE. */
    @Column(name = "nature_activite_precision", length = 255)
    @Size(max = 255)
    private String natureActivitePrecision;

    /** Nouveau reseau, ou extension d'un reseau existant. */
    @Enumerated(EnumType.STRING)
    @Column(name = "portee_reseau", length = 24)
    private PorteeReseau porteeReseau;

    /** Texte libre : duree, echeance ou dates -- le formulaire ne tranche pas. */
    @Column(name = "periode_attribution", length = 255)
    @Size(max = 255)
    private String periodeAttribution;

    // ---------------------------- rubrique 4 ----------------------------

    /**
     * La description du service d'urgence.
     *
     * Le formulaire y consacre pres de deux pages de lignes : c'est LA piece
     * maitresse du dossier, celle sur laquelle l'instruction juge si le
     * service releve bien de l'urgence. D'ou un `text` sans borne plutot
     * qu'un varchar : tronquer a 4 000 caracteres couperait un argumentaire
     * au milieu d'une phrase, sans que l'usager en soit averti.
     */
    @Column(name = "description_service", columnDefinition = "text")
    private String descriptionService;

    /** Le numero souhaite, s'il y en a un. Facultatif. */
    @Column(name = "numero_souhaite", length = 16)
    @Size(max = 16)
    private String numeroSouhaite;

    /** Le numero reellement attribue par l'ARCEP. Vide au depot. */
    @Column(name = "numero_attribue", length = 16)
    @Size(max = 16)
    private String numeroAttribue;

    /** Service public de communications electroniques, ou autre service. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_exploitation", length = 24)
    private TypeExploitation typeExploitation;

    /** Le trafic a ecouler : quatre cases cumulables. */
    @Column(name = "trafic_donnees")
    private Boolean traficDonnees;

    @Column(name = "trafic_voix")
    private Boolean traficVoix;

    @Column(name = "trafic_sms")
    private Boolean traficSms;

    @Column(name = "trafic_autres")
    private Boolean traficAutres;

    /** N'a de sens que si traficAutres est coche. */
    @Column(name = "trafic_autres_precision", length = 255)
    @Size(max = 255)
    private String traficAutresPrecision;

    /** Le point focal, rubrique 4 : quatre champs, a plat. */
    @Column(name = "point_focal_nom", length = 100)
    @Size(max = 100)
    private String pointFocalNom;

    @Column(name = "point_focal_prenoms", length = 100)
    @Size(max = 100)
    private String pointFocalPrenoms;

    @Column(name = "point_focal_email", length = 100)
    @Size(max = 100)
    private String pointFocalEmail;

    @Column(name = "point_focal_telephone", length = 20)
    @Size(max = 20)
    private String pointFocalTelephone;

    /** Reference du titre a renouveler. */
    @Column(name = "reference_autorisation_anterieure", length = 25)
    @Size(max = 25)
    private String referenceAutorisationAnterieure;

    // ---------------------------- rubrique 9 ----------------------------

    /**
     * Frais de dossier retenus, en FCFA.
     *
     * CONSERVES SUR LE DOSSIER et non recalcules a l'affichage : le bareme
     * evolue, et un dossier instruit l'an dernier doit continuer d'afficher le
     * montant qui lui a ete reclame. La valeur est calculee au depot d'apres
     * RefTarif et les types de reseaux declares.
     */
    @Column(name = "frais_dossier", precision = 12, scale = 2)
    private BigDecimal fraisDossier;

    @Column(name = "devise_frais", length = 8)
    @Size(max = 8)
    private String deviseFrais;

    // ------------------------------------------------------------------
    // Le titulaire porte la FK, comme Client le fait pour Asi et pour la
    // demande d'implantation : cette entite n'ajoute donc aucune colonne.
    // ------------------------------------------------------------------

    @OneToOne(mappedBy = "demandeNumeroCourtUrgence", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Client client;

    /**
     * LA personne physique du dossier : le representant de l'operateur, rubrique 2.
     *
     * MOITIE D'UNE PAIRE avec `Client` -- la structure d'un cote, la personne
     * qui agit pour elle de l'autre. C'est ainsi qu'ASI emploie ses deux tables
     * partagees, dans une seule rubrique « Demandeur ».
     *
     * UN-A-UN : le dossier n'a qu'une personne, et pas de tiers.
     *
     * Le nom et les prenoms s'y fondent dans `applicantName`, comme chez ASI :
     * les tables partagees n'ont pas de champ de prenom, et c'est le contrat que
     * le front suit deja pour le demandeur.
     */
    @OneToOne(mappedBy = "demandeNumeroCourtUrgence", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Applicant applicant;

    /** Rubrique 4 : les numeros longs ou fixes de rattachement. */
    @OneToMany(mappedBy = "demandeNumeroCourtUrgence", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<NumeroRattachement> numerosRattachement;

    /**
     * Le rapport technique d'instruction, un par dossier.
     *
     * Remplace le tableau de controles que ce service avait herite du
     * formulaire d'aeronef sans jamais pouvoir le remplir -- voir
     * RapportTechnique. Ouvert au depot, renseigne pendant l'instruction, et
     * jamais saisi par le demandeur.
     */
    @OneToOne(mappedBy = "demandeNumeroCourtUrgence", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private RapportTechnique rapportTechnique;

    /**
     * L'autorisation d'exploitation, delivree pour le reseau ENTIER.
     *
     * Une liste, et non une relation un-a-un : le titre peut etre reemis --
     * renouvellement, correction -- et l'historique doit rester lisible. C'est
     * la meme regle que pour l'implantation (decision metier du 27/08/2026),
     * a ceci pres que la cle de regroupement est le dossier et non la station.
     *
     * Pas de REMOVE ni d'orphanRemoval dans la cascade : un titre delivre est
     * un document officiel, il ne disparait pas avec un nettoyage de dossier.
     * La suppression du dossier est d'ailleurs refusee des qu'une autorisation
     * existe -- voir DemandeNumeroCourtUrgenceService.delete.
     */
    @OneToMany(mappedBy = "demandeNumeroCourtUrgence", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Attestation> attestations;

    // ------------------------------------------------------------------
    // workflow / ACL  (le circuit BPMN ecrit dans ces colonnes)
    // ------------------------------------------------------------------

    @Column(name = "id_post_attachments")
    private String idsPostAttachments;

    @Column(name = "label_post_attachments")
    private String labelsPostAttachments;

    @Column(name = "label_missing_post_attachments")
    private String labelsMissingPostAttachments;

    @Column(name = "signataire")
    private String signataire;

    @Column(name = "signataire_kc_id")
    private String signataireKcId;

    @Column(name = "wf_process_id")
    private String wfProcessID;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "activity_name")
    private String activityName;

    @Column(name = "assignee")
    private String assignee;

    @Column(name = "traited_by")
    private String traitedBy;

    @Column(name = "sid_trated_by")
    private String sidTraitedBy;

    @Column(name = "end_process")
    private Boolean endProcess = false;

    @Column(name = "state", length = 64)
    @Size(max = 64)
    private String state;

    @Column(name = "number_of_attachments")
    private Long numberOfattachments = 0L;

    @Column(name = "step")
    private Long step;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acl_object_identity_id")
    private AclObjectIdentity aclObjectIdentity;

    @PrePersist
    public void generateUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }
}
