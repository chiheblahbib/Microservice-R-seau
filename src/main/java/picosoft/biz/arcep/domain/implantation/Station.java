package picosoft.biz.arcep.domain.implantation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.implantation.enumeration.*;
import picosoft.biz.arcep.client.kernel.model.acl.AclObjectIdentity;
import picosoft.biz.arcep.domain.shared.Attestation;
import picosoft.biz.arcep.domain.shared.enumeration.ApplicantType;
import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "station", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class Station extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "uuid", updatable = false)
    private UUID uuid;

    @Column(name = "reference")
    private String reference;

    @Enumerated(EnumType.STRING)
    @Column(name = "nature_implantation")
    private NatureImplantation natureImplantation;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_station")
    private TypeStation typeStation;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_trafic")
    private TypeTrafic typeTrafic;

    @Enumerated(EnumType.STRING)
    @Column(name = "nature_trafic")
    private NatureTrafic natureTrafic;

    // ---------------------------- support ----------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "nature_support")
    private NatureSupport natureSupport;

    @Column(name = "hauteur_support")
    private Double hauteurSupport;

    @Column(name = "hauteur_totale")
    private Double hauteurTotale;

    // ---------------------------- antenne ----------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "type_antenne")
    private TypeAntenne typeAntenne;

    @Enumerated(EnumType.STRING)
    @Column(name = "positionnement_antenne")
    private PositionnementAntenne positionnement;

    // -------------------------- installateur -------------------------
    //
    // L'installateur est un acteur de meme nature que le demandeur : il peut
    // etre une societe ou un particulier, et l'instruction a besoin des memes
    // elements pour l'identifier. Les colonnes reprennent donc, une a une,
    // celles de Client et Applicant -- memes longueurs, meme decoupage.
    //
    // Elles restent PLATES sur station plutot que de pointer vers un Client :
    // Client est rattache au dossier (demande_implantation_id) et porte des
    // champs qui n'ont pas de sens ici (usage de l'equipement, mode de
    // transport). Le reutiliser aurait force ces colonnes a rester vides et
    // brouille la lecture de la table.

    /** COMPANY ou INDIVIDUAL, comme pour le demandeur. */
    @Enumerated(EnumType.STRING)
    @Column(name = "installateur_type")
    private ApplicantType installateurType;

    /** Le pendant de client.company. */
    @Column(name = "installateur_raison_sociale")
    private String installateurRaisonSociale;

    @Column(name = "installateur_adresse")
    private String installateurAdresse;

    @Column(name = "installateur_registre_commerce")
    private String installateurRegistreCommerce;

    @Column(name = "installateur_nature_activite")
    private String installateurNatureActivite;

    /** Le pendant de applicant.applicantName : la personne qui installe. */
    @Column(name = "installateur_identite")
    private String installateurIdentite;

    @Column(name = "installateur_nationalite")
    private String installateurNationalite;

    /** Le libelle du pays, stocke a cote du code, comme chez le demandeur. */
    @Column(name = "installateur_nationalite_complet")
    private String installateurNationaliteComplet;

    @Column(name = "installateur_qualification")
    private String installateurQualification;

    @Column(name = "installateur_telephone")
    private String installateurTelephone;

    @Column(name = "installateur_email")
    private String installateurEmail;

    /** Pilote l'eclatement du dossier parent en circuits enfants. */
    @Enumerated(EnumType.STRING)
    @Column(name = "statut_station")
    private StatutStation statutStation;

    /**
     * Le dossier qui porte cette station, en un-a-un.
     *
     * La colonne ne change pas. La contrainte d'unicite qui rend la regle
     * physique -- uq_station_demande_implantation -- a ete posee a la main :
     * ddl-auto=update ne l'ajoute pas sur une colonne existante. Voir
     * local-setup.sql, ou elle figure pour toute base recreee de zero.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_implantation_id")
    private DemandeImplantation demandeImplantation;

    @OneToOne(mappedBy = "station", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private SiteImplantation siteImplantation;

    @OneToMany(mappedBy = "station", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<Frequence> frequences;

    /** L'autorisation d'implantation est delivree par station. */
    @OneToMany(mappedBy = "station", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
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

    /** Couvre aussi les cascades : une Station creee via le dossier parent passe ici. */
    @PrePersist
    public void generateUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }
}
