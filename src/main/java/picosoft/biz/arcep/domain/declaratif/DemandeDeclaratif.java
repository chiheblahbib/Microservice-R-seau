package picosoft.biz.arcep.domain.declaratif;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.shared.Applicant;
import picosoft.biz.arcep.domain.drrrs.RapportTechnique;
import picosoft.biz.arcep.domain.declaratif.enumeration.*;
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
 * Declaration des activites relevant du regime declaratif.
 *
 * Reprend le formulaire de l'ARCEP : identite de l'exploitant (1),
 * correspondants obligatoires (2 et 3), enregistrement (4), clientele cible
 * (5), couverture geographique (6), services a valeur ajoutee (7),
 * infrastructures de reseau (8), tarification (9).
 *
 *
 * UNE DECLARATION, PAS UNE DEMANDE D'AUTORISATION
 *
 * C'est le seul des dix dossiers du module qui ne SOLLICITE rien : l'exploitant
 * DECLARE une activite qu'il a le droit d'exercer, et l'ARCEP lui delivre un
 * certificat d'enregistrement. Le circuit reste le meme -- depot, instruction,
 * cloture -- mais son livrable est un enregistrement, non une autorisation.
 *
 *
 * DEUX CORRESPONDANTS OBLIGATOIRES, ET NON UN
 *
 * Les rubriques 2 et 3 exigent DEUX contacts distincts : celui de la
 * declaration, et celui du paiement. Le formulaire les marque tous deux d'un
 * asterisque. D'ou une collection de personnes portant un role, comme le
 * dossier de navire, et non un representant unique comme l'aeronef.
 *
 *
 * PAS D'ANNEXE TARIFAIRE, MAIS DES FRAIS MENTIONNES
 *
 * La liste des pieces se termine par « 19) Frais d'etudes du dossier », sans
 * montant. On ne pose donc rien : le principe des frais est acquis, leur
 * bareme non. Voir DemandeDeclaratifService.arreterFraisDossier.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "demande_declaratif", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class DemandeDeclaratif extends Auditable implements Serializable {

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

    /**
     * Nature de la demande.
     *
     * ABSENTE DU FORMULAIRE, qui pose la question autrement -- voir
     * `typeEnregistrement`, rubrique 4. Conservee pour la plomberie du module,
     * dont les ecrans de suivi trient dessus.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "nature_demande")
    private NatureDemande natureDemande;

    /** Nouvelle declaration, ou modification d'un certificat existant. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_enregistrement")
    private TypeEnregistrement typeEnregistrement;

    /** Le numero du certificat modifie. N'a de sens qu'en modification. */
    @Column(name = "numero_certificat")
    private String numeroCertificat;

    /**
     * Le certificat d'enregistrement delivre a l'issue de l'instruction.
     *
     * Vide au depot : c'est le livrable du dossier, pas une donnee saisie.
     */
    @Column(name = "certificat_delivre")
    private String certificatDelivre;

    // ---------------------------- rubrique 1 ----------------------------
    // La denomination, l'adresse, la boite postale et le RCCM sont portes par
    // `client`, la table partagee. Restent ici les deux champs qu'elle n'a pas.

    /** Forme juridique : SARL, SA, etablissement public... */
    @Column(name = "forme_juridique")
    private String formeJuridique;

    /** Boite postale, que `client` ne porte pas separement de l'adresse. */
    @Column(name = "boite_postale")
    private String boitePostale;

    // ---------------------------- rubrique 5 ----------------------------
    // Trois cases cumulables : un exploitant peut viser le grand public ET les
    // entreprises. Des booleens plutot qu'une table, parce qu'il n'y en a que
    // trois et qu'aucune ne porte d'information a cote.

    @Column(name = "clientele_grand_public")
    private Boolean clienteleGrandPublic;

    @Column(name = "clientele_professionnels")
    private Boolean clienteleProfessionnels;

    @Column(name = "clientele_operateurs")
    private Boolean clienteleOperateurs;

    // ---------------------------- rubrique 6 ----------------------------

    /** Province, ou tout le territoire national. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type_couverture")
    private TypeCouverture typeCouverture;

    /**
     * Les provinces couvertes, quand la couverture n'est pas nationale.
     *
     * Chaine et non table : le Gabon en compte neuf, la liste ne bougera pas,
     * et personne ne cherchera un dossier par province avant longtemps. Le
     * jour ou ce sera le cas, une table sera justifiee -- pas avant.
     */
    @Column(name = "provinces")
    private String provinces;

    // ---------------------------- rubrique 8 ----------------------------
    // Les questions b a e sont des « Disposez-vous de... ? Oui / Non », chacune
    // suivie de « Si oui, decrire brievement ». Un booleen a trois etats et un
    // texte pour chacune : null distingue « pas encore repondu » de « non ».

    @Column(name = "reseau_collecte_hertzien")
    private Boolean reseauCollecteHertzien;

    @Column(name = "reseau_collecte_filaire")
    private Boolean reseauCollecteFilaire;

    @Column(name = "reseau_collecte_description", columnDefinition = "text")
    private String reseauCollecteDescription;

    @Column(name = "coeur_reseau_propre")
    private Boolean coeurReseauPropre;

    @Column(name = "coeur_reseau_description", columnDefinition = "text")
    private String coeurReseauDescription;

    @Column(name = "plateformes_propres")
    private Boolean plateformesPropres;

    @Column(name = "plateformes_description", columnDefinition = "text")
    private String plateformesDescription;

    @Column(name = "produits_de_gros")
    private Boolean produitsDeGros;

    @Column(name = "produits_de_gros_description", columnDefinition = "text")
    private String produitsDeGrosDescription;

    // ---------------------------- rubrique 9 ----------------------------

    /**
     * Le tarif applique aux usagers.
     *
     * Texte et non montant, comme pour l'USSD : c'est une grille commerciale,
     * pas une somme due a l'ARCEP.
     */
    @Column(name = "tarif_usagers", columnDefinition = "text")
    private String tarifUsagers;

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

    @OneToOne(mappedBy = "demandeDeclaratif", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Client client;

    /**
     * LA personne physique du dossier : les correspondants de la declaration (rubrique 2) et
     * du paiement (rubrique 3) designent la meme.
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
    @OneToOne(mappedBy = "demandeDeclaratif", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Applicant applicant;

    /** Rubrique 7 : les services declares, avec leur calendrier. */
    @OneToMany(mappedBy = "demandeDeclaratif", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<ServiceDeclare> services;

    /** Rubrique 8.a : les caracteristiques de reseau declarees. */
    @OneToMany(mappedBy = "demandeDeclaratif", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<InfrastructureDeclaree> infrastructures;

    /**
     * Le rapport technique d'instruction, un par dossier.
     *
     * Remplace le tableau de controles que ce service avait herite du
     * formulaire d'aeronef sans jamais pouvoir le remplir -- voir
     * RapportTechnique. Ouvert au depot, renseigne pendant l'instruction, et
     * jamais saisi par le demandeur.
     */
    @OneToOne(mappedBy = "demandeDeclaratif", fetch = FetchType.LAZY,
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
     * existe -- voir DemandeDeclaratifService.delete.
     */
    @OneToMany(mappedBy = "demandeDeclaratif", fetch = FetchType.LAZY,
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
