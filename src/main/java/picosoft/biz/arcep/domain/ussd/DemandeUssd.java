package picosoft.biz.arcep.domain.ussd;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.shared.Applicant;
import picosoft.biz.arcep.domain.drrrs.RapportTechnique;
import picosoft.biz.arcep.domain.ussd.enumeration.*;
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
 * Demande d'attribution de codes USSD.
 *
 * Reprend le formulaire de l'ARCEP : operateur (1), representant (2), nature
 * de l'activite (3), renseignements techniques (4), codes sollicites (5),
 * description des services (6), tarif aux usagers (7), pieces (8),
 * engagement (9).
 *
 *
 * LA RUBRIQUE 4 EST UN BILAN, PAS UNE DEMANDE
 *
 * « Nombre de codes attribues / en cours d'utilisation / taux d'utilisation » :
 * l'operateur declare ce qu'il a DEJA, et l'instruction s'en sert pour juger
 * si de nouveaux codes se justifient. Le taux est stocke tel qu'il est
 * declare, sans etre recalcule : c'est une DECLARATION de l'operateur, et la
 * comparer au quotient des deux autres colonnes fait partie de l'instruction.
 *
 *
 * PAS D'ANNEXE TARIFAIRE
 *
 * Ce formulaire ne porte aucun montant a payer a l'ARCEP. En revanche il
 * demande, rubrique 7, le TARIF APPLIQUE AUX USAGERS : ce n'est pas une
 * redevance mais une information commerciale, d'ou une colonne de texte et non
 * un montant.
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
@Table(name = "demande_ussd", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class DemandeUssd extends Auditable implements Serializable {

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

    /** Mobile, fixe, service a valeur ajoutee, ou autre. */
    @Enumerated(EnumType.STRING)
    @Column(name = "nature_activite")
    private NatureActivite natureActivite;

    /** N'a de sens que si natureActivite vaut AUTRE. */
    @Column(name = "nature_activite_precision")
    private String natureActivitePrecision;

    // ---------------------------- rubrique 4 ----------------------------
    // Le bilan de l'existant -- voir l'en-tete de classe.

    @Column(name = "codes_attribues")
    private Integer codesAttribues;

    @Column(name = "codes_en_utilisation")
    private Integer codesEnUtilisation;

    /**
     * Taux d'utilisation declare, en pourcentage.
     *
     * Stocke TEL QU'IL EST DECLARE et non recalcule depuis les deux colonnes
     * ci-dessus : c'est une declaration de l'operateur, et l'ecart eventuel
     * avec le quotient est precisement ce que l'instruction regarde.
     */
    @Column(name = "taux_utilisation", precision = 5, scale = 2)
    private java.math.BigDecimal tauxUtilisation;

    // ---------------------------- rubrique 5 ----------------------------

    @Column(name = "nombre_codes_sollicites")
    private Integer nombreCodesSollicites;

    /**
     * Format souhaite des codes, tel que l'operateur le decrit.
     *
     * Texte libre : le formulaire ouvre une colonne « Format du ou des codes
     * sollicites » sans en fixer la syntaxe. « *123# », « 4 chiffres » et
     * « court, 3 niveaux » y sont tous recevables.
     */
    @Column(name = "format_codes")
    private String formatCodes;

    // ---------------------------- rubrique 6 ----------------------------

    /**
     * La description des services offerts sur les codes.
     *
     * Douze lignes au formulaire : `text` sans borne, pour la meme raison que
     * la description du service d'urgence -- tronquer couperait un
     * argumentaire au milieu d'une phrase.
     */
    @Column(name = "description_services", columnDefinition = "text")
    private String descriptionServices;

    // ---------------------------- rubrique 7 ----------------------------

    /**
     * Le tarif applique AUX USAGERS.
     *
     * Une chaine et non un montant : ce n'est pas une somme unique mais une
     * grille -- « 50 F par consultation, gratuit pour le solde ». Un decimal
     * aurait force a n'en retenir qu'une ligne.
     */
    @Column(name = "tarif_usagers", columnDefinition = "text")
    private String tarifUsagers;

    /** Reference du titre a renouveler. */
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

    @Column(name = "devise_frais")
    private String deviseFrais;

    // ------------------------------------------------------------------
    // Le titulaire porte la FK, comme Client le fait pour Asi et pour la
    // demande d'implantation : cette entite n'ajoute donc aucune colonne.
    // ------------------------------------------------------------------

    @OneToOne(mappedBy = "demandeUssd", fetch = FetchType.LAZY,
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
    @OneToOne(mappedBy = "demandeUssd", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Applicant applicant;

    /** Rubrique 5 : les codes USSD sollicites, par preference. */
    @OneToMany(mappedBy = "demandeUssd", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<CodeUssd> codes;

    /**
     * Le rapport technique d'instruction, un par dossier.
     *
     * Remplace le tableau de controles que ce service avait herite du
     * formulaire d'aeronef sans jamais pouvoir le remplir -- voir
     * RapportTechnique. Ouvert au depot, renseigne pendant l'instruction, et
     * jamais saisi par le demandeur.
     */
    @OneToOne(mappedBy = "demandeUssd", fetch = FetchType.LAZY,
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
     * existe -- voir DemandeUssdService.delete.
     */
    @OneToMany(mappedBy = "demandeUssd", fetch = FetchType.LAZY,
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
