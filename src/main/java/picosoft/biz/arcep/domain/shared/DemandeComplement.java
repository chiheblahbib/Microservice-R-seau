package picosoft.biz.arcep.domain.shared;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.client.kernel.model.acl.AclObjectIdentity;
import picosoft.biz.arcep.configuration.audit.Auditable;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "demande_complement", schema = "homologation")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class DemandeComplement extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID uuid;

    @Column(name = "reference", length = 25)
    private String reference;

    @Column(name = "reference_dossier", length = 25)
    private String referenceDossier;

    @Column(name = "subject", length = 255)
    private String subject;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "created_date", length = 25)
    private ZonedDateTime createdDate;

    @Column(name = "sended_date", length = 25)
    private ZonedDateTime sendedDate;

    @Column(name = "approvedBy")
    private String approvedBy;

    @Column(name = "categorie", length = 200)
    private String categorie;

    @Column(name = "id_post_attachments")
    private String idsPostAttachments;

    @Column(name = "label_post_attachments")
    private String labelsPostAttachments;

    @Column(name = "label_missing_post_attachments")
    private String labelsMissingPostAttachments;

    @Column(name = "wf_process_id")
    private String wfProcessID;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "class_id_dossier")
    private Long classIdDossier;

    @Column(name = "object_id_dossier")
    private Long objectIdDossier;

    @Column(name = "activity_name")
    private String activityName;

    @Column(name = "assignee")
    private String assignee;

    @Column(name = "end_process")
    private Boolean endProcess = false;

    @Column(name = "state", length = 64)
    private String state;

    @Column(name = "state_demande", length = 64)
    private String stateDemande;

    @Column(name = "number_of_attachments")
    private Long numberOfattachments = 0L;

    @Column(name = "step")
    private Long step;

    @Column(name = "affected_sid", length = 255)
    private String affectedSid;

    @Column(name = "affected_name", length = 255)
    private String affectedName;

    @Column(name = "affected_keycloak_id", length = 255)
    private String affectedKeycloakId;

    @Column(name = "sid_externe", length = 255)
    private String sidExterne;

    @Column(name = "delai_reponse")
    private Boolean delaiReponse;

    @Column(name = "web")
    private Boolean web;

    @Column(name = "validation")
    private Boolean validation;

    @Column(name = "commentaire")
    private String commentaire;

    @Column(name = "nom_intervenant", length = 255)
    private String nomIntervenant;

    @Column(name = "type_dossier", length = 50)
    private String typeDossier;

    /**
     * Colonne partagee avec homologation (dossier ASI parent). Le back drrrs ne
     * connait pas l'entite Asi : il la garde en simple identifiant, et rattache
     * ses propres dossiers par typeDossier + objectIdDossier.
     */
    @Column(name = "asi_id")
    private Long asiId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acl_object_identity_id")
    @org.hibernate.annotations.Index(name = "identity_demande_complement_id_index")
    private AclObjectIdentity aclObjectIdentity;

    @OneToOne(mappedBy = "demandeComplement", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private ReponseDemandeComplement reponseDemandeComplement;

    @OneToOne(mappedBy = "demandeComplement", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Destinataire destinataire;

    @PrePersist
    public void generateUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }
}
