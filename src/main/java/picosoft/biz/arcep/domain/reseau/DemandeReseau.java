package picosoft.biz.arcep.domain.reseau;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.reseau.enumeration.*;
import picosoft.biz.arcep.client.kernel.model.acl.AclObjectIdentity;
import picosoft.biz.arcep.domain.shared.Attestation;
import picosoft.biz.arcep.domain.shared.Applicant;
import picosoft.biz.arcep.domain.shared.Client;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Demande d'autorisation d'etablissement et d'exploitation d'un reseau de
 * communications electroniques.
 *
 * Reprend le formulaire de l'ARCEP du 26 janvier 2026, rubrique par rubrique.
 *
 *
 * TROIS ACTEURS, ET NON DEUX
 *
 * La ou le dossier d'implantation oppose un operateur a un demandeur, celui-ci
 * distingue :
 *   - le TITULAIRE du reseau, une societe -> Client, table partagee ;
 *   - le REQUERANT, personne physique qui depose ;
 *   - le RESPONSABLE du reseau, personne physique qui en repond.
 *
 * Les deux vivent desormais dans `applicant`, la table partagee, et se
 * distinguent par leur ROLE -- REQUERANT, RESPONSABLE.
 *
 * Elles y ont longtemps echappe pour une raison qui ne tient plus : le
 * formulaire exige d'elles une PIECE D'IDENTITE, type et numero, que la table
 * partagee ne porte pas. On ne l'y a pas ajoutee -- le document reste demande
 * comme piece jointe, mais son type et son numero ne sont plus saisis.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "demande_reseau", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class DemandeReseau extends Auditable implements Serializable {

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

    // ---------------------------- rubrique 4 ----------------------------

    /** Prive ou ouvert au public : commande six pieces supplementaires. */
    @Enumerated(EnumType.STRING)
    @Column(name = "nature_reseau")
    private NatureReseau natureReseau;

    // ---------------------------- rubrique 5 ----------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "nature_demande")
    private NatureDemande natureDemande;

    /**
     * Reference de l'autorisation precedente, exigee en cas de renouvellement.
     * Le formulaire demande d'en joindre une copie ; on garde ici de quoi la
     * retrouver.
     */
    @Column(name = "reference_autorisation_anterieure")
    private String referenceAutorisationAnterieure;

    /**
     * Conclusion de l'Etude Technique, saisie par le technicien et envoyee avec sa
     * decision « Pour Validation ». Le rapport technique la lit dans `data` : il est
     * produit par l'evenement kernel RapportTechniqueReseau sur cette transition,
     * comme la decharge sur l'« Accepter » de la Numerotation.
     */
    @Column(name = "conclusion", length = 4000)
    private String conclusion;

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

    // ---------------------------- rubrique 10 ---------------------------

    /** Engagement sur l'honneur : qui signe, et en quelle qualite. */
    @Column(name = "engagement_nom")
    private String engagementNom;

    @Column(name = "engagement_qualite")
    private String engagementQualite;

    @Column(name = "engagement_lieu")
    private String engagementLieu;

    @Column(name = "engagement_date")
    private ZonedDateTime engagementDate;

    // ------------------------------------------------------------------
    // Le titulaire porte la FK, comme Client le fait pour Asi et pour la
    // demande d'implantation : cette entite n'ajoute donc aucune colonne.
    // ------------------------------------------------------------------

    @OneToOne(mappedBy = "demandeReseau", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Client client;

    /**
     * LA personne physique du dossier : le requerant (rubrique 1) et le responsable du
     * reseau (rubrique 3) designent la meme.
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
    @OneToOne(mappedBy = "demandeReseau", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Applicant applicant;

    @OneToMany(mappedBy = "demandeReseau", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<TypeReseauDeclare> typesReseau;

    @OneToMany(mappedBy = "demandeReseau", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<ServiceDeclare> services;

    @OneToMany(mappedBy = "demandeReseau", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<SiteReseau> sites;

    @OneToMany(mappedBy = "demandeReseau", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<LiaisonReseau> liaisons;

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
     * existe -- voir DemandeReseauService.delete.
     */
    @OneToMany(mappedBy = "demandeReseau", fetch = FetchType.LAZY,
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
