package picosoft.biz.arcep.domain.pq;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.shared.Applicant;
import picosoft.biz.arcep.domain.shared.RapportTechnique;
import picosoft.biz.arcep.domain.pq.enumeration.*;
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
 * Demande de bloc de numeros (BPQ).
 *
 * Reprend le formulaire de l'ARCEP : operateur (1), representant (2), nature
 * de l'activite (3), ressource sollicitee (4), engagement (5).
 *
 *
 * DEUX SENS DE CIRCULATION DANS UN SEUL FORMULAIRE
 *
 * La rubrique 3 fait choisir entre « une restitution » et « une demande
 * d'attribution ». Le meme imprime sert donc a DEMANDER des blocs et a en
 * RENDRE -- deux instructions opposees. `typeOperation` porte la distinction ;
 * un booleen l'aurait fait lire comme l'absence de l'autre.
 *
 *
 * PAS D'ANNEXE TARIFAIRE
 *
 * Contrairement au numero court (4 a 10 millions de redevance annuelle), ce
 * formulaire ne porte aucun montant. Voir DemandePqService.arreterFraisDossier.
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
@Table(name = "demande_pq", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class DemandePq extends Auditable implements Serializable {

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

    /**
     * Nature de la demande : nouvelle, modification, renouvellement.
     *
     * ABSENTE DU FORMULAIRE, conservee pour la plomberie du module -- les
     * ecrans de suivi trient dessus. A ne pas confondre avec `typeOperation`,
     * qui est la vraie question de la rubrique 3.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "nature_demande", length = 16)
    private NatureDemande natureDemande;

    /** Rubrique 3, premiere question : mobile ou fixe. */
    @Enumerated(EnumType.STRING)
    @Column(name = "nature_activite", length = 24)
    private NatureActivite natureActivite;

    /** Rubrique 3, seconde question : attribution ou restitution. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_operation", length = 16)
    private TypeOperationPq typeOperation;

    /**
     * Le rapport d'utilisation des blocs existants.
     *
     * Le formulaire le reclame « en cas d'extension » et le range parmi les
     * pieces. On garde ici de quoi dire qu'il a ete fourni et ce qu'il dit en
     * substance : l'instruction s'appuie dessus pour juger si de nouveaux
     * blocs se justifient.
     */
    @Column(name = "etat_utilisation_blocs", columnDefinition = "text")
    private String etatUtilisationBlocs;

    /**
     * Reference du titre a renouveler ou des blocs restitues.
     *
     * Absente du formulaire ; conservee pour la meme raison que
     * `natureDemande`, et parce qu'une restitution porte necessairement sur
     * une attribution anterieure qu'il faut pouvoir retrouver.
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

    @OneToOne(mappedBy = "demandePq", fetch = FetchType.LAZY,
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
    @OneToOne(mappedBy = "demandePq", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Applicant applicant;

    /** Rubrique 4 : les blocs BPQ sollicites, ou restitues. */
    @OneToMany(mappedBy = "demandePq", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<BlocNumeros> blocs;

    /**
     * Le rapport technique d'instruction, un par dossier.
     *
     * Remplace le tableau de controles que ce service avait herite du
     * formulaire d'aeronef sans jamais pouvoir le remplir -- voir
     * RapportTechnique. Ouvert au depot, renseigne pendant l'instruction, et
     * jamais saisi par le demandeur.
     */
    @OneToOne(mappedBy = "demandePq", fetch = FetchType.LAZY,
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
     * existe -- voir DemandePqService.delete.
     */
    @OneToMany(mappedBy = "demandePq", fetch = FetchType.LAZY,
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
