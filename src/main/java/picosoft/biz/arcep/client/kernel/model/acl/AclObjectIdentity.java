package picosoft.biz.arcep.client.kernel.model.acl;


import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "acl_object_identity", schema = "public")
@Audited
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

public class AclObjectIdentity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JsonIgnore
    @JoinColumn(name = "object_id_class")
    private AclClass objectIdClass;

//    @OneToOne
    @Column(name = "object_id_identity")
    private String objectIdentity;

    @Column
    private Integer parent_object;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JoinColumn(name = "owner_sid")
    @org.hibernate.annotations.Index(name = "sidIdentity_id_index") // Adding an index
    private AclSid ownerSid;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name= "acl_object_identity")
    // Hibernate regroupera les identifiants des entités enfants à charger
    // dans une seule requête SQL en utilisant une sous-requête.
    @Fetch(FetchMode.SUBSELECT)
    private Set<AclEntry> aclEntries=new HashSet<>();

    @Column(name = "entries_inheriting")
    private boolean entriesInheriting;

    private String anomalieDescription;
    private ZonedDateTime anomalieDate;

    public String getAnomalieDescription() {
        return anomalieDescription;
    }

    public void setAnomalieDescription(String anomalieDescription) {
        this.anomalieDescription = anomalieDescription;
    }

    public ZonedDateTime getAnomalieDate() {
        return anomalieDate;
    }

    public void setAnomalieDate(ZonedDateTime anomalieDate) {
        this.anomalieDate = anomalieDate;
    }

    public String getObjectIdentity() {
        return objectIdentity;
    }

    public void setObjectIdentity(String objectIdentity) {
        this.objectIdentity = objectIdentity;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

   public Integer getParent_object() {
        return parent_object;
    }

    public void setParent_object(Integer parent_object) {
        this.parent_object = parent_object;
    }

    public AclClass getObjectIdClass() {
        return objectIdClass;
    }

    public void setObjectIdClass(AclClass objectIdClass) {
        this.objectIdClass = objectIdClass;
    }


    public AclSid getOwnerSid() {
        return ownerSid;
    }

    public void setOwnerSid(AclSid ownerSid) {
        this.ownerSid = ownerSid;
    }

    public boolean isEntriesInheriting() {
        return entriesInheriting;
    }

    public void setEntriesInheriting(boolean entriesInheriting) {
        this.entriesInheriting = entriesInheriting;
    }

    public Set<AclEntry> getAclEntries() {
        return aclEntries;
    }

    public void setAclEntries(Set<AclEntry> aclEntries) {
        this.aclEntries = aclEntries;
    }
}

