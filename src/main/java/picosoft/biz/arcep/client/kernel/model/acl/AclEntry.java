package picosoft.biz.arcep.client.kernel.model.acl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Entity
@Table(name = "acl_entry", schema = "public")
@Audited
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

public class AclEntry implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @JsonIgnore
    @JoinColumn(name = "acl_object_identity")
    @org.hibernate.annotations.Index(name = "identityEntry_id_index") // Adding an index
        private AclObjectIdentity aclObjectIdentity;

        @Column
        private Integer ace_order;

        @OneToOne(fetch = FetchType.LAZY)
        @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
        @JsonIgnore
        @JoinColumn(name="sid")
        @org.hibernate.annotations.Index(name = "sidEntry_id_index") // Adding an index
        private AclSid sid;

        @Column
        @org.hibernate.annotations.Index(name = "mask_entry_index") // Adding an index
        private Integer mask;
        @Column
        private Boolean granting;
        @Column
        private Boolean audit_success;
        @Column
        private Boolean audit_failure;

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

        public Integer getId() {
                return id;
        }

        public void setId(Integer id) {
                this.id = id;
        }

        public AclObjectIdentity getAclObjectIdentity() {
                return aclObjectIdentity;
        }

        public void setAclObjectIdentity(AclObjectIdentity aclObjectIdentity) {
                this.aclObjectIdentity = aclObjectIdentity;
        }

        public Integer getAce_order() {
                return ace_order;
        }

        public void setAce_order(Integer ace_order) {
                this.ace_order = ace_order;
        }

        public AclSid getSid() {
                return sid;
        }

        public void setSid(AclSid sid) {
                this.sid = sid;
        }

        public Integer getMask() {
                return mask;
        }

        public void setMask(Integer mask) {
                this.mask = mask;
        }

        public Boolean getGranting() {
                return granting;
        }

        public void setGranting(Boolean granting) {
                this.granting = granting;
        }

        public Boolean getAudit_success() {
                return audit_success;
        }

        public void setAudit_success(Boolean audit_success) {
                this.audit_success = audit_success;
        }

        public Boolean getAudit_failure() {
                return audit_failure;
        }

        public void setAudit_failure(Boolean audit_failure) {
                this.audit_failure = audit_failure;
        }
}
