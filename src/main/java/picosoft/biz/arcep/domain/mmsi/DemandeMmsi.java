package picosoft.biz.arcep.domain.mmsi;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.shared.RapportTechnique;
import picosoft.biz.arcep.domain.mmsi.enumeration.*;
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
 * Demande de numero d'identification mobile maritime (MMSI) pour stations
 * radioelectriques de navire.
 *
 * Reprend le formulaire de l'ARCEP : demandeur (1), representant (2),
 * caracteristiques de la station (3), pieces (4), engagement (5).
 *
 *
 * QUATRE FAMILLES DE STATIONS, ET NON UNE
 *
 * Malgre son titre, ce formulaire ne sert pas qu'aux navires : sa rubrique 3
 * est decoupee en quatre blocs -- stations de navire (A), stations cotieres
 * (B), aeronefs utilisant des identites du service mobile maritime (C), et
 * aides a la navigation equipees d'AIS (D). Un MMSI est demande pour l'une
 * OU l'autre, jamais pour plusieurs. D'ou `categorie`, portee separement des
 * cases cochees : sans elle, deux dossiers cochant « appel simultane d'un
 * groupe » seraient indistinguables.
 *
 *
 * PAS D'ANNEXE TARIFAIRE
 *
 * Le formulaire n'en porte pas, alors que celui du navire -- dont il partage
 * la liste de pieces mot pour mot -- facture 200 000 FCFA plus 700 000 par an.
 * On ne pose donc aucun montant ici.
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
@Table(name = "demande_mmsi", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class DemandeMmsi extends Auditable implements Serializable {

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

    /** Nouvelle demande, modification, renouvellement. */
    @Enumerated(EnumType.STRING)
    @Column(name = "nature_demande", length = 16)
    private NatureDemande natureDemande;

    /** A, B, C ou D -- voir l'en-tete de classe. */
    @Enumerated(EnumType.STRING)
    @Column(name = "categorie", length = 24)
    private CategorieStation categorie;

    /**
     * Le nom et l'immatriculation du navire, quand il y en a un.
     *
     * ABSENTS DES RUBRIQUES : le formulaire ne nomme jamais le navire en
     * clair, il exige seulement son certificat d'immatriculation en piece
     * jointe. Ajoutes parce qu'un MMSI est attribue A UN MOBILE PRECIS et
     * qu'aucune colonne ne permettrait autrement de dire lequel.
     *
     * Facultatifs, et pas seulement par prudence : les categories C et D ne
     * portent pas sur un navire du tout.
     */
    @Column(name = "nom_mobile", length = 100)
    @Size(max = 100)
    private String nomMobile;

    @Column(name = "immatriculation_mobile", length = 32)
    @Size(max = 32)
    private String immatriculationMobile;

    /**
     * Le MMSI attribue par l'ARCEP.
     *
     * Vide au depot : c'est le resultat du dossier. Neuf chiffres dont les
     * trois premiers sont l'indicatif de pays (626 pour le Gabon) -- mais
     * stocke en CHAINE, parce que ces zeros de tete comptent et qu'un MMSI ne
     * se calcule jamais.
     */
    @Column(name = "mmsi_attribue", length = 16)
    @Size(max = 16)
    private String mmsiAttribue;

    /** Reference du titre a renouveler. */
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

    @OneToOne(mappedBy = "demandeMmsi", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Client client;

    /**
     * Le representant de l'operateur, rubrique 2. Un seul, d'ou le un-a-un.
     */
    @OneToOne(mappedBy = "demandeMmsi", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private PersonneMmsi representant;

    /** Rubrique 3 : les cases cochees, dans le bloc de la categorie. */
    @OneToMany(mappedBy = "demandeMmsi", fetch = FetchType.LAZY,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<BesoinMmsi> besoins;

    /**
     * Le rapport technique d'instruction, un par dossier.
     *
     * Remplace le tableau de controles que ce service avait herite du
     * formulaire d'aeronef sans jamais pouvoir le remplir -- voir
     * RapportTechnique. Ouvert au depot, renseigne pendant l'instruction, et
     * jamais saisi par le demandeur.
     */
    @OneToOne(mappedBy = "demandeMmsi", fetch = FetchType.LAZY,
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
     * existe -- voir DemandeMmsiService.delete.
     */
    @OneToMany(mappedBy = "demandeMmsi", fetch = FetchType.LAZY,
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
