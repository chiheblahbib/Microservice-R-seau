package picosoft.biz.arcep.domain.aeronef;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.shared.RapportTechnique;
import picosoft.biz.arcep.domain.aeronef.enumeration.*;
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
 * Demande d'autorisation d'exploitation d'une station radioelectrique a bord
 * d'un aeronef.
 *
 * Reprend le formulaire de l'ARCEP rubrique par rubrique : operateur (1),
 * representant (2), nature de la demande (3), situation anterieure (4),
 * declaration de la station (5), trafic (6), verifications (7), pieces (8),
 * frais (9), engagement (10).
 *
 *
 * DEUX ACTEURS
 *
 *   - l'OPERATEUR ou EXPLOITANT -> Client, table partagee avec homologation ;
 *   - son REPRESENTANT, une personne physique -> PersonneAeronef, un-a-un.
 *
 * Le formulaire n'en demande qu'un seul, a la difference du reseau qui
 * distingue requerant et responsable. La relation est donc un-a-un, et non une
 * collection : une collection aurait laisse croire qu'on peut en saisir
 * plusieurs, et il aurait fallu inventer un role pour les distinguer.
 *
 *
 * IMMATRICULATION : UN CHAMP QUE LE FORMULAIRE N'A PAS
 *
 * `immatriculationAeronef` NE FIGURE PAS sur le formulaire papier. Il est
 * ajoute ici parce que sans lui rien n'identifie l'appareil couvert par
 * l'autorisation, alors que les frais sont dus « par aeronef » et que le
 * certificat d'immatriculation de l'ANAC est une piece exigee. A confirmer
 * avec le metier : si l'ARCEP se contente de la piece jointe, le champ peut
 * etre rendu facultatif -- il l'est deja techniquement.
 *
 *
 * LES VERIFICATIONS NE SONT PAS SAISIES PAR LE DEMANDEUR
 *
 * La rubrique 7 est un tableau de controles (date, lieu, visa, observations)
 * que l'ARCEP remplit pendant l'instruction, pas au depot. Elle est modelisee
 * comme les autres, mais l'ecran de saisie ne doit pas la proposer a l'usager.
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
@Table(name = "demande_aeronef", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class DemandeAeronef extends Auditable implements Serializable {

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

    @Enumerated(EnumType.STRING)
    @Column(name = "nature_demande", length = 16)
    private NatureDemande natureDemande;

    /**
     * L'immatriculation de l'appareil.
     *
     * Absente du formulaire papier -- voir l'en-tete de classe. Facultative
     * tant que le metier n'a pas tranche.
     */
    @Column(name = "immatriculation_aeronef", length = 32)
    @Size(max = 32)
    private String immatriculationAeronef;

    // ---------------------------- rubrique 4 ----------------------------

    /**
     * « Possediez-vous auparavant un aeronef equipe de radio ? »
     *
     * Trois etats et non deux : null signifie que la question n'a pas ete
     * repondue, ce qu'un boolean primitif aurait confondu avec « non ».
     */
    @Column(name = "possedait_aeronef_radio")
    private Boolean possedaitAeronefRadio;

    // ---------------------------- rubrique 5 ----------------------------

    /** Restriction de parcours : IFR ou FR. */
    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_parcours", length = 8)
    private RestrictionParcours restrictionParcours;

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
    @Column(name = "trafic_autres_precision", length = 255)
    @Size(max = 255)
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

    @OneToOne(mappedBy = "demandeAeronef", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Client client;

    /**
     * Le representant de l'operateur, rubrique 2. Un seul, d'ou le un-a-un.
     */
    @OneToOne(mappedBy = "demandeAeronef", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private PersonneAeronef representant;

    /** Rubrique 5 : le tableau des equipements de bord. */
    @OneToMany(mappedBy = "demandeAeronef", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<EquipementBord> equipements;

    /**
     * Rubrique 7 : les controles, remplis par l'ARCEP pendant l'instruction.
     * Voir l'en-tete de classe -- l'ecran de depot ne les propose pas.
     */
    @OneToMany(mappedBy = "demandeAeronef", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<VerificationControle> verifications;

    /**
     * Le rapport d'instruction, un par dossier.
     *
     * DISTINCT des verifications ci-dessus, et non leur remplacant : celles-ci
     * sont un journal de controles, rubrique 7 de l'imprime ; celui-la est la
     * conclusion de l'instruction, qui ne vient d'aucune rubrique. Voir
     * RapportTechnique.
     */
    @OneToOne(mappedBy = "demandeAeronef", fetch = FetchType.LAZY,
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
     * existe -- voir DemandeAeronefService.delete.
     */
    @OneToMany(mappedBy = "demandeAeronef", fetch = FetchType.LAZY,
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
