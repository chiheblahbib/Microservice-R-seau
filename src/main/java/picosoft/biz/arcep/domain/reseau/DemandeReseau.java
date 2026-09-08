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
import javax.validation.constraints.Size;
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
 * Les deux personnes vivent dans PersonneReseau et non dans la table partagee
 * `applicant` : le formulaire exige d'elles une piece d'identite -- type et
 * numero -- que cette table ne porte pas, et l'y ajouter modifierait une
 * structure qu'homologation partage.
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

    // ---------------------------- rubrique 4 ----------------------------

    /** Prive ou ouvert au public : commande six pieces supplementaires. */
    @Enumerated(EnumType.STRING)
    @Column(name = "nature_reseau", length = 24)
    private NatureReseau natureReseau;

    // ---------------------------- rubrique 5 ----------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "nature_demande", length = 16)
    private NatureDemande natureDemande;

    /**
     * Reference de l'autorisation precedente, exigee en cas de renouvellement.
     * Le formulaire demande d'en joindre une copie ; on garde ici de quoi la
     * retrouver.
     */
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

    // ---------------------------- rubrique 10 ---------------------------

    /** Engagement sur l'honneur : qui signe, et en quelle qualite. */
    @Column(name = "engagement_nom", length = 100)
    @Size(max = 100)
    private String engagementNom;

    @Column(name = "engagement_qualite", length = 100)
    @Size(max = 100)
    private String engagementQualite;

    @Column(name = "engagement_lieu", length = 100)
    @Size(max = 100)
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
     * Le REQUERANT, rubrique 1 -- la personne qui demande.
     *
     * Porte par `Applicant`, la table partagee, comme le fait ASI pour son
     * demandeur et l'implantation pour le sien. La rubrique 1 du formulaire
     * et celle d'ASI demandent la meme chose : qui demande, en quelle
     * qualite, et comment le joindre.
     *
     * Le formulaire demande en plus la NATURE ET LE NUMERO DE SA PIECE
     * D'IDENTITE. `Applicant` ne les porte pas, et on n'ajoute rien aux
     * tables partagees : le document reste exige comme PIECE JOINTE -- il
     * figure a la liste des pieces du dossier -- mais son type et son numero
     * ne sont plus saisis.
     */
    @OneToOne(mappedBy = "demandeReseau", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Applicant applicant;

    /**
     * Le RESPONSABLE DU RESEAU, rubrique 3.
     *
     * Reste une `PersonneReseau` : ce n'est pas le demandeur mais un tiers,
     * qui repond techniquement du reseau. La collection garde son role pour
     * que la rubrique 1, desormais portee par `Applicant`, n'y figure plus.
     */
    @OneToMany(mappedBy = "demandeReseau", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<PersonneReseau> personnes;

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
