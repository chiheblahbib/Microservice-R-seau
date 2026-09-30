package picosoft.biz.arcep.domain.numerocourt;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.shared.Applicant;
import picosoft.biz.arcep.domain.drrrs.RapportTechnique;
import picosoft.biz.arcep.domain.numerocourt.enumeration.*;
import picosoft.biz.arcep.client.kernel.model.acl.AclObjectIdentity;
import picosoft.biz.arcep.domain.shared.Attestation;
import picosoft.biz.arcep.domain.shared.Client;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Demande d'attribution d'un numero court.
 *
 * Reprend le formulaire de l'ARCEP : operateur (1), representant (2), nature
 * de l'activite (3), specification de services (4), pieces (5),
 * engagement (6).
 *
 *
 * LE TYPE DE NUMERO COMMANDE LE TARIF
 *
 * L'annexe ne connait que deux lignes :
 *
 *     Classique   4 000 000 FCFA de redevance annuelle
 *     Gold       10 000 000 FCFA de redevance annuelle
 *
 * C'est la SEULE donnee du formulaire dont depend un montant. Voir
 * DemandeNumeroCourtService.arreterFraisDossier.
 *
 *
 * DEUX QUESTIONS LIEES, RUBRIQUE 4
 *
 * `typeFacturation` n'a de sens que si `modeExploitation` vaut NORMAL : un
 * numero vert ne facture pas l'appelant. Le lien est verifie a la soumission
 * plutot qu'impose par le schema -- une contrainte en base aurait refuse les
 * brouillons a moitie remplis, que le formulaire autorise.
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
@Table(name = "demande_numerocourt", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class DemandeNumeroCourt extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "uuid", updatable = false)
    private UUID uuid;

    @Column(name = "web")
    private Boolean web;

    @Column(name = "reference")
    private String reference;

    @Column(name = "created_date")
    private ZonedDateTime createdDate;

    @Column(name = "sended_date")
    private ZonedDateTime sendedDate;

    @Column(name = "approvedBy")
    private String approvedBy;

    /**
     * Compare tel quel par les passerelles du circuit : ${data.statutDossier == 'soumis'}.
     * Chaine et non enumeration, pour s'aligner sur Asi et sur les diagrammes
     * deployes, qui testent des chaines nues. La valeur est posee par le front.
     */
    @Column(name = "statut_dossier")
    private String statutDossier;

    @Column(name = "type_dossier")
    private String typeDossier;

    // ---------------------------- rubrique 3 ----------------------------

    /** Nouvelle demande, modification, renouvellement. */
    @Enumerated(EnumType.STRING)
    @Column(name = "nature_demande")
    private NatureDemande natureDemande;

    /** Mobile, fixe, ou autre reseau / service. */
    @Enumerated(EnumType.STRING)
    @Column(name = "nature_activite")
    private NatureActivite natureActivite;

    /** N'a de sens que si natureActivite vaut AUTRE. */
    @Column(name = "nature_activite_precision")
    private String natureActivitePrecision;

    /** Nouveau reseau, ou extension d'un reseau existant. */
    @Enumerated(EnumType.STRING)
    @Column(name = "portee_reseau")
    private PorteeReseau porteeReseau;

    /**
     * Periode d'attribution souhaitee.
     *
     * TEXTE LIBRE : le formulaire ouvre une ligne sans preciser s'il attend
     * une duree (« 5 ans »), une echeance (« jusqu'en 2031 ») ou des dates.
     * Deux colonnes de dates auraient impose une lecture que le formulaire ne
     * prescrit pas et perdu les deux autres reponses.
     */
    @Column(name = "periode_attribution")
    private String periodeAttribution;

    // ---------------------------- rubrique 4 ----------------------------

    /** Classique (8XYZ) ou gold (8X8X). Commande le tarif. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_numero")
    private TypeNumeroCourt typeNumero;

    /**
     * Le numero court demande, si l'operateur en souhaite un en particulier.
     *
     * ABSENT DES RUBRIQUES : le formulaire fait declarer le TYPE mais ne
     * demande pas quel numero precis. Ajoute parce qu'un operateur exprime en
     * pratique une preference, et que la refuser obligerait a la noter en
     * marge. Facultatif.
     */
    @Column(name = "numero_souhaite")
    private String numeroSouhaite;

    /** Le numero reellement attribue par l'ARCEP. Vide au depot. */
    @Column(name = "numero_attribue")
    private String numeroAttribue;

    /** Service public de communications electroniques, ou autre service. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_exploitation")
    private TypeExploitation typeExploitation;

    /** Numero normal (appels payants) ou numero vert. */
    @Enumerated(EnumType.STRING)
    @Column(name = "mode_exploitation")
    private ModeExploitation modeExploitation;

    /** N'a de sens que si modeExploitation vaut NORMAL -- voir l'en-tete. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_facturation")
    private TypeFacturation typeFacturation;

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
    @Column(name = "trafic_autres_precision")
    private String traficAutresPrecision;

    /**
     * Le point focal, rubrique 4 : quatre champs seulement -- nom, prenoms,
     * courriel, telephone.
     *
     * A PLAT sur le dossier et non dans `applicant` : le formulaire n'en
     * demande ni fonction, ni nationalite, ni adresse, et une ligne de la
     * table partagee aurait presente six colonnes vides a remplir. Ce n'est
     * pas un acteur du dossier mais un point de contact technique.
     */
    @Column(name = "point_focal_nom")
    private String pointFocalNom;

    @Column(name = "point_focal_prenoms")
    private String pointFocalPrenoms;

    @Column(name = "point_focal_email")
    private String pointFocalEmail;

    @Column(name = "point_focal_telephone")
    private String pointFocalTelephone;

    /**
     * Reference du titre a renouveler.
     *
     * Le formulaire reclame « copie de la decision en cas de demande de
     * renouvellement » parmi les pieces ; on garde ici de quoi la retrouver.
     */
    @Column(name = "reference_autorisation_anterieure")
    private String referenceAutorisationAnterieure;

    /**
     * Redevance annuelle retenue, en FCFA.
     *
     * Le seul montant de ce formulaire : il n'y a pas de frais de dossier a
     * l'annexe. `fraisDossier` reste donc vide, et c'est cette colonne qui
     * porte le tarif.
     */
    @Column(name = "redevance_annuelle", precision = 12, scale = 2)
    private java.math.BigDecimal redevanceAnnuelle;

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

    @Column(name = "devise_frais")
    private String deviseFrais;

    // ------------------------------------------------------------------
    // Le titulaire porte la FK, comme Client le fait pour Asi et pour la
    // demande d'implantation : cette entite n'ajoute donc aucune colonne.
    // ------------------------------------------------------------------

    @OneToOne(mappedBy = "demandeNumeroCourt", fetch = FetchType.LAZY,
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
    @OneToOne(mappedBy = "demandeNumeroCourt", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Applicant applicant;

    /** Rubrique 4 : les numeros longs ou fixes de rattachement. */
    @OneToMany(mappedBy = "demandeNumeroCourt", fetch = FetchType.LAZY,
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
    @OneToOne(mappedBy = "demandeNumeroCourt", fetch = FetchType.LAZY,
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
     * existe -- voir DemandeNumeroCourtService.delete.
     */
    @OneToMany(mappedBy = "demandeNumeroCourt", fetch = FetchType.LAZY,
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

    @Column(name = "state")
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
