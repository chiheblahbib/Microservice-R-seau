package picosoft.biz.arcep.domain.implantation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.implantation.enumeration.*;
import picosoft.biz.arcep.client.kernel.model.acl.AclObjectIdentity;
import picosoft.biz.arcep.domain.shared.Applicant;
import picosoft.biz.arcep.domain.shared.Client;
import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "demande_implantation", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class DemandeImplantation extends Auditable implements Serializable {

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
     * Teste tel quel par la gateway du circuit parent : ${data.statutDossier== 'soumis'}.
     * String et non enumeration, pour s'aligner sur Asi.java:130 et sur les diagrammes
     * deployes, qui comparent a des chaines nues. La valeur est posee par le front.
     */
    @Column(name = "statut_dossier", length = 32)
    @Size(max = 32)
    private String statutDossier;

    /** Oriente la branche de signature : Direction Commerce ou Chef Centre. */
    @Column(name = "type_dossier", length = 50)
    @Size(max = 50)
    private String typeDossier;

    // ------------------------------------------------------------------
    // Client et Applicant : la FK est portee par la table enfant, comme
    // pour Homologation et Asi. Cette entite n'ajoute donc aucune colonne.
    // ------------------------------------------------------------------

    @OneToOne(mappedBy = "demandeImplantation", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Client client;

    /**
     * LA personne physique du dossier : le demandeur.
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
    @OneToOne(mappedBy = "demandeImplantation", fetch = FetchType.LAZY,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Applicant applicant;

    /**
     * LA station du dossier. Une autorisation d'implantation en vise une seule.
     *
     * La cle etrangere reste portee par l'enfant (station.demande_implantation_id),
     * exactement comme Client et Applicant ci-dessus : passer de @OneToMany a
     * @OneToOne(mappedBy=...) ne change donc aucune colonne.
     */
    @OneToOne(mappedBy = "demandeImplantation", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Station station;

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

    /** Couvre aussi les cascades : une Station creee via le dossier parent passe ici. */
    @PrePersist
    public void generateUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }
}
