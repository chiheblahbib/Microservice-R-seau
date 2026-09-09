package picosoft.biz.arcep.domain.ispc;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.shared.Applicant;
import picosoft.biz.arcep.domain.shared.RapportTechnique;
import picosoft.biz.arcep.domain.ispc.enumeration.*;
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
 * Demande d'attribution d'un code ISPC -- point semaphore international.
 *
 * Reprend le formulaire de l'ARCEP rubrique par rubrique : identite du
 * demandeur (1 a 5), fonctions dans le reseau (6), point semaphore local
 * (7 a 10), point semaphore distant (11 a 13), engagement (14).
 *
 *
 * LE PLUS COURT DES NEUF FORMULAIRES
 *
 * Deux pages, quatorze rubriques numerotees, aucun tableau et AUCUNE ANNEXE
 * TARIFAIRE. Tout ce que le gabarit de l'aeronef portait et qui n'a pas
 * d'objet ici a ete retire plutot que laisse a null : des colonnes
 * `trafic_voix` ou `restriction_parcours` sur un dossier de signalisation SS7
 * auraient fini par etre remplies par quelqu'un.
 *
 * Il n'y a pas non plus de frais : `fraisDossier` reste, herite de la
 * plomberie commune, mais aucun bareme ne l'alimente -- voir
 * DemandeIspcService.arreterFraisDossier.
 *
 *
 * POURQUOI LE DEMANDEUR N'A QU'UNE PERSONNE A CONTACTER
 *
 * La rubrique 5 dit « Personne a contacter » et rien de plus : ni fonction, ni
 * nationalite, ni adresse propre. `Applicant`, la table partagee ou vit
 * desormais cette personne, en porte pourtant : c'est la forme d'ASI et
 * d'homologation. L'ecran de saisie n'en propose que le nom, le telephone et
 * le courriel ; les colonnes en trop restent vides.
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
@Table(name = "demande_ispc", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class DemandeIspc extends Auditable implements Serializable {

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

    // ------------------------- rubriques 1 a 5 --------------------------
    // La raison sociale, l'adresse, le telephone et le courriel sont portes
    // par `client`, la table partagee avec l'homologation. La personne a
    // contacter est portee par `contact`, plus bas.

    /**
     * Nature de la demande : nouvelle, modification, renouvellement.
     *
     * ABSENTE DU FORMULAIRE, qui ne pose pas la question. Conservee parce que
     * toute la plomberie du module la lit -- les ecrans de suivi trient
     * dessus -- et parce qu'un code ISPC se renouvelle en pratique. Le
     * formulaire de saisie la propose avec NOUVEAU par defaut.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "nature_demande", length = 16)
    private NatureDemande natureDemande;

    /**
     * Reference du titre a renouveler.
     *
     * Absente du formulaire, comme `natureDemande` dont elle depend : la
     * fiche ISPC ne prevoit pas le renouvellement. Conservee parce que le
     * module en propose un, et qu'un renouvellement sans reference du titre
     * precedent oblige l'instructeur a le chercher a la main.
     */
    @Column(name = "reference_autorisation_anterieure", length = 25)
    @Size(max = 25)
    private String referenceAutorisationAnterieure;

    // ---------------------------- rubrique 7 ----------------------------

    /** Fabricant et type du point semaphore, en un seul champ comme au formulaire. */
    @Column(name = "fabricant_type_semaphore", length = 200)
    @Size(max = 200)
    private String fabricantTypeSemaphore;

    // ---------------------------- rubrique 8 ----------------------------

    /** Adresse PHYSIQUE du point semaphore : ou la machine se trouve. */
    @Column(name = "adresse_physique_semaphore", length = 255)
    @Size(max = 255)
    private String adressePhysiqueSemaphore;

    // ---------------------------- rubrique 9 ----------------------------

    /**
     * Date de mise en service, que le formulaire demande au MOIS pres
     * (« mois/annee »).
     *
     * Stockee en date complete malgre tout, au premier du mois : une colonne
     * texte « 09/2026 » ne se trierait ni ne se comparerait, et le jour n'a
     * de toute facon jamais ete demande.
     */
    @Column(name = "date_mise_en_service")
    private ZonedDateTime dateMiseEnService;

    // --------------------------- rubrique 10 ----------------------------

    /**
     * « Identification d'au moins un lieu semaphore MPT en projet ».
     *
     * Texte libre : le formulaire ouvre une zone sans structure, et decouper
     * en lieux nommes imposerait une forme qu'il ne prescrit pas.
     */
    @Column(name = "lieu_semaphore_mpt", columnDefinition = "text")
    private String lieuSemaphoreMpt;

    // ------------------------ rubriques 11 a 13 -------------------------
    // Le point semaphore DISTANT, celui avec lequel le demandeur veut
    // dialoguer. Un seul au formulaire, d'ou des colonnes plutot qu'une table.

    @Column(name = "semaphore_distant_nom_adresse", length = 255)
    @Size(max = 255)
    private String semaphoreDistantNomAdresse;

    @Column(name = "semaphore_distant_emplacement", length = 255)
    @Size(max = 255)
    private String semaphoreDistantEmplacement;

    /**
     * Le code ISPC du point distant, « s'il est connu » -- donc facultatif,
     * le formulaire le dit lui-meme.
     */
    @Column(name = "semaphore_distant_code_ispc", length = 32)
    @Size(max = 32)
    private String semaphoreDistantCodeIspc;

    /**
     * Le code attribue par l'ARCEP a l'issue de l'instruction.
     *
     * Vide au depot : c'est le resultat du dossier, pas une donnee saisie.
     * Range ici et non sur une entite separee parce qu'il n'y en a qu'un par
     * dossier -- contrairement aux blocs de numeros du dossier PQ.
     */
    @Column(name = "code_ispc_attribue", length = 32)
    @Size(max = 32)
    private String codeIspcAttribue;

    // --------------------------- rubrique 14 ----------------------------

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

    @OneToOne(mappedBy = "demandeIspc", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Client client;

    /**
     * TOUTES les personnes physiques du dossier, quel que soit leur role.
     *
     * `Applicant` est la table partagee, celle d'ASI et d'homologation. Le role
     * -- REQUERANT, RESPONSABLE, REPRESENTANT... -- est porte par la LIGNE, non
     * par une relation : une personne de plus dans une rubrique n'oblige alors
     * ni a une colonne ni a une migration.
     *
     * Le nom et les prenoms s'y fondent dans `applicantName`, comme chez ASI :
     * les tables partagees n'ont pas de champ de prenom, et c'est le contrat que
     * le front suit deja pour le demandeur.
     */
    @OneToMany(mappedBy = "demandeIspc", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<Applicant> applicants;

    /** Rubrique 6 : les fonctions cochees, cumulables. */
    @OneToMany(mappedBy = "demandeIspc", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<FonctionPointSemaphore> fonctions;

    /**
     * Le rapport technique d'instruction, un par dossier.
     *
     * Remplace le tableau de controles que ce service avait herite du
     * formulaire d'aeronef sans jamais pouvoir le remplir -- voir
     * RapportTechnique. Ouvert au depot, renseigne pendant l'instruction, et
     * jamais saisi par le demandeur.
     */
    @OneToOne(mappedBy = "demandeIspc", fetch = FetchType.LAZY,
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
     * existe -- voir DemandeIspcService.delete.
     */
    @OneToMany(mappedBy = "demandeIspc", fetch = FetchType.LAZY,
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
