package picosoft.biz.arcep.domain.installateur;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.shared.Applicant;
import picosoft.biz.arcep.domain.shared.RapportTechnique;
import picosoft.biz.arcep.domain.installateur.enumeration.*;
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
 * Demande d'autorisation d'installateur, distributeur ou sous-distributeur
 * d'equipements et d'infrastructures de communications electroniques.
 *
 * Reprend le formulaire de l'ARCEP : identite de l'entreprise, du
 * representant et du requerant ; nature de la demande (1), etendue de
 * l'activite (2), renseignements complementaires (3), responsable de
 * l'activite (4), techniciens specialistes (5), locaux (6), moyens materiels
 * (7), zone geographique (8), activites anterieures (9), pieces (10),
 * frais (11).
 *
 *
 * TROIS QUALITES CUMULABLES, ET UN TARIF PAR QUALITE
 *
 * La rubrique 1 est une liste de cases, et l'annexe parle d'« Installateur
 * ET/OU Distributeur ». Un dossier peut donc porter deux qualites. La grille :
 *
 *     Installateur et/ou Distributeur   frais de dossier      100 000 FCFA
 *     Installateur                      redevance annuelle  2 000 000 FCFA
 *     Distributeur                      redevance annuelle  1 000 000 FCFA
 *     Sous-Distributeur                 frais de dossier       50 000 FCFA
 *     Sous-Distributeur                 redevance annuelle    150 000 FCFA
 *
 * Les frais sont « payables lors du depot », la redevance « communiquee par
 * l'ARCEP apres validation du dossier » -- deux moments distincts, comme pour
 * le navire. Voir DemandeInstallateurService.arreterFraisDossier pour ce que
 * l'annexe ne dit PAS.
 *
 *
 * TROIS PERSONNES POSSIBLES
 *
 * Le representant legal, le requerant (« a remplir si son identite differe de
 * celle du representant ») et le responsable de l'activite sollicitee
 * (rubrique 4), qui porte en plus une qualification et une experience. Une
 * collection portant un role, comme le navire et le declaratif.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "demande_installateur", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class DemandeInstallateur extends Auditable implements Serializable {

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

    // ---------------------------- rubrique 1 ----------------------------

    /**
     * Nouvelle demande, ou renouvellement.
     *
     * La case « Renouvellement de l'autorisation » figure sur le papier dans
     * la meme liste que les trois qualites, mais elle n'en est pas une : c'est
     * ce que le dossier FAIT. Elle est donc portee ici et non dans
     * `qualites` -- les melanger aurait produit un dossier « de qualite
     * renouvellement », qui ne veut rien dire.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "nature_demande", length = 16)
    private NatureDemande natureDemande;

    /** Reference du titre a renouveler, que le formulaire fait joindre. */
    @Column(name = "reference_autorisation_anterieure", length = 25)
    @Size(max = 25)
    private String referenceAutorisationAnterieure;

    // ------------------------ identite de l'entreprise ------------------
    // La denomination, l'adresse, le telephone, le RCCM et le site web sont
    // portes par `client`. Restent les trois champs qu'il n'a pas.

    @Column(name = "forme_juridique", length = 100)
    @Size(max = 100)
    private String formeJuridique;

    /** Numero d'identification fiscale, propre a ce formulaire. */
    @Column(name = "nif", length = 50)
    @Size(max = 50)
    private String nif;

    @Column(name = "boite_postale", length = 50)
    @Size(max = 50)
    private String boitePostale;

    /**
     * Localisation du siege, distincte de l'adresse postale.
     *
     * Le formulaire demande les DEUX sur des lignes separees : « Adresse » et
     * « Localisation ». La premiere sert au courrier, la seconde a trouver les
     * locaux -- et la rubrique 6 exige d'ailleurs un plan de localisation.
     */
    @Column(name = "localisation", length = 255)
    @Size(max = 255)
    private String localisation;

    // ---------------------------- rubrique 2 ----------------------------

    /** N'a de sens que si l'etendue AUTRE est cochee. */
    @Column(name = "etendue_autre_precision", length = 255)
    @Size(max = 255)
    private String etendueAutrePrecision;

    // ---------------------------- rubrique 3 ----------------------------
    // Deux questions oui / non. Trois etats : null vaut « non repondu ».

    @Column(name = "permanence_accueil")
    private Boolean permanenceAccueil;

    @Column(name = "repondeur_enregistreur")
    private Boolean repondeurEnregistreur;

    // ---------------------------- rubrique 6 ----------------------------

    /**
     * Surface des locaux reservee a l'activite, en metres carres.
     *
     * Decimal : c'est une vraie mesure, qui se compare et se totalise -- a la
     * difference des puissances d'equipement, que le formulaire de l'aeronef
     * laisse en texte faute d'unite prescrite. Ici l'unite ne fait pas de
     * doute.
     */
    @Column(name = "surface_locaux", precision = 10, scale = 2)
    private java.math.BigDecimal surfaceLocaux;

    // ---------------------------- rubrique 7 ----------------------------

    /**
     * Les vehicules affectes a l'activite, rubrique 7-1.
     *
     * Texte libre : le formulaire ouvre « 7-1 VEHICULE » sans colonnes ni
     * cases. Une table aurait impose une structure -- marque, immatriculation,
     * annee -- que rien ne demande.
     */
    @Column(name = "vehicules", columnDefinition = "text")
    private String vehicules;

    // ---------------------------- rubrique 8 ----------------------------

    /** La zone geographique : Libreville est propose a part sur le papier. */
    @Column(name = "zone_libreville")
    private Boolean zoneLibreville;

    @Column(name = "autres_localites", length = 255)
    @Size(max = 255)
    private String autresLocalites;

    /** Liste et adresses des agences assurant un service permanent local. */
    @Column(name = "agences", columnDefinition = "text")
    private String agences;

    // ---------------------------- rubrique 9 ----------------------------

    /** Autres activites exercees avant la presente demande. */
    @Column(name = "activites_anterieures", columnDefinition = "text")
    private String activitesAnterieures;

    // --------------------------- rubrique 11 ----------------------------

    /**
     * Redevance annuelle retenue, en FCFA.
     *
     * Portee sur le dossier mais non facturee ici : l'annexe la dit
     * « communiquee par l'ARCEP apres validation du dossier ». Comme pour le
     * navire, on enregistre le montant applicable au depot et c'est le circuit
     * qui la declenchera.
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

    @Column(name = "devise_frais", length = 8)
    @Size(max = 8)
    private String deviseFrais;

    // ------------------------------------------------------------------
    // Le titulaire porte la FK, comme Client le fait pour Asi et pour la
    // demande d'implantation : cette entite n'ajoute donc aucune colonne.
    // ------------------------------------------------------------------

    @OneToOne(mappedBy = "demandeInstallateur", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Client client;

    /**
     * LA personne physique du dossier : le representant legal, le requerant et le
     * responsable de l'activite (rubrique 4) designent la meme.
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
    @OneToOne(mappedBy = "demandeInstallateur", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Applicant applicant;

    /** Rubrique 1 : les qualites demandees, cumulables. */
    @OneToMany(mappedBy = "demandeInstallateur", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<QualiteDemandee> qualites;

    /** Rubrique 2 : l'etendue de l'activite, cumulable. */
    @ElementCollection(targetClass = EtendueActivite.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "etendue_activite", schema = "drrrs",
                     joinColumns = @JoinColumn(name = "demande_installateur_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "etendue", length = 32)
    private java.util.Set<EtendueActivite> etendues;

    /** Rubrique 5 : les techniciens specialistes de la profession. */
    @OneToMany(mappedBy = "demandeInstallateur", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<TechnicienSpecialiste> techniciens;

    /** Rubrique 7-2 : l'outillage professionnel declare. */
    @OneToMany(mappedBy = "demandeInstallateur", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<OutillageDeclare> outillages;

    /**
     * Le rapport technique d'instruction, un par dossier.
     *
     * Remplace le tableau de controles que ce service avait herite du
     * formulaire d'aeronef sans jamais pouvoir le remplir -- voir
     * RapportTechnique. Ouvert au depot, renseigne pendant l'instruction, et
     * jamais saisi par le demandeur.
     */
    @OneToOne(mappedBy = "demandeInstallateur", fetch = FetchType.LAZY,
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
     * existe -- voir DemandeInstallateurService.delete.
     */
    @OneToMany(mappedBy = "demandeInstallateur", fetch = FetchType.LAZY,
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
