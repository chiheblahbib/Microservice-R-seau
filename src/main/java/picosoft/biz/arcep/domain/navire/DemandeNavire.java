package picosoft.biz.arcep.domain.navire;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.shared.Applicant;
import picosoft.biz.arcep.domain.drrrs.RapportTechnique;
import picosoft.biz.arcep.domain.navire.enumeration.*;
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
 * Demande d'autorisation d'exploitation d'une station radioelectrique a bord
 * d'un navire.
 *
 * Reprend le formulaire de l'ARCEP rubrique par rubrique : demandeur (1),
 * proprietaire du navire (2), nature de la demande (3), situation anterieure
 * (4), declaration de la station -- implantation 5.1, station 5.2,
 * equipements 5.3 --, pieces (6), engagement (7).
 *
 *
 * TROIS ECARTS AVEC LE DOSSIER D'AERONEF, DONT CETTE CLASSE EST ISSUE
 *
 *   - UNE LOCALISATION. Le navire se deplace, mais sa station est declaree a
 *     un lieu (rubrique 5.1) : province, localite, quartier, coordonnees GPS.
 *     L'aeronef n'en a aucune.
 *   - UN PROPRIETAIRE DISTINCT. La rubrique 2 n'est a remplir que si le
 *     demandeur n'est pas le proprietaire. Deux personnes possibles, donc,
 *     contre une seule pour l'aeronef.
 *   - UNE REDEVANCE ANNUELLE. L'annexe tarifaire porte 200 000 FCFA de frais
 *     de dossier ET 700 000 FCFA par an, la seconde « payable apres que
 *     l'ARCEP a juge le dossier conforme ». L'aeronef n'a que le forfait.
 *
 *
 * DEUX ACTEURS
 *
 *   - l'OPERATEUR ou EXPLOITANT -> Client, table partagee avec homologation ;
 *   - les PERSONNES PHYSIQUES du dossier -> Applicant, table partagee, une
 *     collection ou chacune porte son role.
 *
 * Une collection et non deux relations un-a-un : le demandeur (rubrique 1) et
 * le proprietaire (rubrique 2) ont exactement les memes champs, et la seconde
 * rubrique ne se remplit que s'ils different. Un `role` les distingue.
 *
 *
 * IMMATRICULATION : UN CHAMP QUE LE FORMULAIRE N'A PAS
 *
 * `immatriculationNavire` NE FIGURE PAS sur le formulaire papier. Il est
 * ajoute ici parce que sans lui rien n'identifie l'appareil couvert par
 * l'autorisation, alors que les frais sont dus « par navire » et que le
 * certificat d'immatriculation de l'ANAC est une piece exigee. A confirmer
 * avec le metier : si l'ARCEP se contente de la piece jointe, le champ peut
 * etre rendu facultatif -- il l'est deja techniquement.
 *
 *
 * LE RAPPORT TECHNIQUE N'EST PAS UNE RUBRIQUE DU FORMULAIRE
 *
 * Ce dossier portait un tableau de controles herite du formulaire d'aeronef,
 * dont l'imprime du navire ne parle pas : la table est restee vide. C'est le
 * rapport d'instruction qui la remplace -- voir RapportTechnique --, et il ne
 * vient d'aucune rubrique parce qu'il consigne ce que l'ARCEP conclut, non ce
 * que le demandeur declare. L'ecran de depot ne le propose pas.
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
@Table(name = "demande_navire", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class DemandeNavire extends Auditable implements Serializable {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "nature_demande")
    private NatureDemande natureDemande;

    /** Le nom du navire, rubrique 3. */
    @Column(name = "nom_navire")
    private String nomNavire;

    /**
     * L'immatriculation de l'appareil.
     *
     * Absente du formulaire papier -- voir l'en-tete de classe. Facultative
     * tant que le metier n'a pas tranche.
     */
    @Column(name = "immatriculation_navire")
    private String immatriculationNavire;

    // ---------------------------- rubrique 4 ----------------------------

    /**
     * « Possediez-vous auparavant un navire equipe de radio ? »
     *
     * Trois etats et non deux : null signifie que la question n'a pas ete
     * repondue, ce qu'un boolean primitif aurait confondu avec « non ».
     */
    @Column(name = "possedait_navire_radio")
    private Boolean possedaitNavireRadio;

    /**
     * Le numero de l'autorisation deja detenue, si la question ci-dessus a
     * recu « oui ». Distinct de referenceAutorisationAnterieure, qui sert au
     * renouvellement : ici on decrit une situation passee, la un dossier.
     */
    @Column(name = "numero_autorisation")
    private String numeroAutorisation;

    // --------------------------- rubrique 5.1 ---------------------------
    // Le lieu d'implantation de la station. L'aeronef n'a pas cette rubrique :
    // le navire se deplace, mais sa station est declaree a une adresse.

    @Column(name = "province")
    private String province;

    @Column(name = "localite")
    private String localite;

    @Column(name = "quartier")
    private String quartier;

    /**
     * Coordonnees en degres decimaux.
     *
     * BigDecimal et non double : six decimales valent une dizaine de
     * centimetres, et un flottant binaire ne represente pas exactement la
     * valeur saisie. Le bareme des distances se calcule dessus.
     */
    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    /** Motif de l'implantation : creation, extension, modification, autre. */
    @Enumerated(EnumType.STRING)
    @Column(name = "motif_implantation")
    private MotifImplantation motifImplantation;

    /** N'a de sens que si le motif est AUTRE. */
    @Column(name = "motif_implantation_precision")
    private String motifImplantationPrecision;

    // --------------------------- rubrique 5.2 ---------------------------

    /** Type de station : satellite, hertzienne, autre. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_station")
    private TypeStation typeStation;

    /** N'a de sens que si le type est AUTRE. */
    @Column(name = "type_station_precision")
    private String typeStationPrecision;

    /**
     * Bande de frequences exploitee, en texte libre.
     *
     * Le formulaire laisse une ligne, sans format impose : on la garde telle
     * quelle plutot que d'inventer un couple de bornes que l'imprime ne
     * demande pas.
     */
    @Column(name = "bande_frequences_exploitee")
    private String bandeFrequencesExploitee;

    // ---------------------------- rubrique 6 ----------------------------

    /**
     * Type de trafic a ecouler : le formulaire propose trois cases a cocher,
     * cumulables, et non un choix unique.
     */
    @Column(name = "trafic_voix")
    private Boolean traficVoix;

    @Column(name = "trafic_donnees")
    private Boolean traficDonnees;

    @Column(name = "trafic_autres")
    private Boolean traficAutres;

    /** N'a de sens que si traficAutres est coche. */
    @Column(name = "trafic_autres_precision")
    private String traficAutresPrecision;

    /** Nature du trafic : national et international sont cumulables. */
    @Column(name = "trafic_national")
    private Boolean traficNational;

    @Column(name = "trafic_international")
    private Boolean traficInternational;

    /**
     * Reference de l'autorisation precedente, exigee en cas de renouvellement.
     * Le formulaire demande d'en joindre une copie ; on garde ici de quoi la
     * retrouver.
     */
    @Column(name = "reference_autorisation_anterieure")
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

    /**
     * Redevance annuelle d'exploitation, 700 000 FCFA a l'annexe tarifaire.
     *
     * Portee separement des frais de dossier parce qu'elle n'est pas due au
     * meme moment : l'annexe la dit « payable apres que l'ARCEP a juge le
     * dossier conforme », quand les 200 000 FCFA de frais accompagnent le
     * depot. Deux echeances, donc deux colonnes.
     */
    @Column(name = "redevance_annuelle", precision = 12, scale = 2)
    private BigDecimal redevanceAnnuelle;

    @Column(name = "devise_frais")
    private String deviseFrais;

    // ------------------------------------------------------------------
    // Le titulaire porte la FK, comme Client le fait pour Asi et pour la
    // demande d'implantation : cette entite n'ajoute donc aucune colonne.
    // ------------------------------------------------------------------

    @OneToOne(mappedBy = "demandeNavire", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Client client;

    /**
     * LA personne physique du dossier : le demandeur (rubrique 1) et le proprietaire du
     * navire (rubrique 2) designent la meme.
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
    @OneToOne(mappedBy = "demandeNavire", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Applicant applicant;

    /**
     * Rubrique 4 : les autorisations deja obtenues, une par ligne.
     *
     * Le formulaire laisse plusieurs lignes ; on ne les concatene pas dans un
     * champ texte, sans quoi il faudrait les redecouper pour les afficher.
     */
    @OneToMany(mappedBy = "demandeNavire", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<AutorisationAnterieure> autorisationsAnterieures;

    /** Rubrique 5 : le tableau des equipements de bord. */
    @OneToMany(mappedBy = "demandeNavire", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<EquipementBord> equipements;

    /**
     * Le rapport technique d'instruction, un par dossier.
     *
     * Remplace le tableau de controles que ce service avait herite du
     * formulaire d'aeronef sans jamais pouvoir le remplir -- voir
     * RapportTechnique. Ouvert au depot, renseigne pendant l'instruction, et
     * jamais saisi par le demandeur.
     */
    @OneToOne(mappedBy = "demandeNavire", fetch = FetchType.LAZY,
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
     * existe -- voir DemandeNavireService.delete.
     */
    @OneToMany(mappedBy = "demandeNavire", fetch = FetchType.LAZY,
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
