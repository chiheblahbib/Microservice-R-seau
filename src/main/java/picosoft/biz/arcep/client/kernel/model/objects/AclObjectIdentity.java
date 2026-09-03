package picosoft.biz.arcep.client.kernel.model.objects;


import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import picosoft.biz.arcep.client.kernel.model.acl.AclSid;

import java.time.ZonedDateTime;

public class AclObjectIdentity {

    private Long id;

    private AclClass objectIdClass;

    private String objectIdentity;

    private Integer parent_object;

    private AclSid ownerSid;

    private boolean entriesInheriting;

    private String anomalieDescription;

    private ZonedDateTime anomalieDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AclClass getObjectIdClass() {
        return objectIdClass;
    }

    public void setObjectIdClass(AclClass objectIdClass) {
        this.objectIdClass = objectIdClass;
    }

    public String getObjectIdentity() {
        return objectIdentity;
    }

    public void setObjectIdentity(String objectIdentity) {
        this.objectIdentity = objectIdentity;
    }

    public Long getObjectLongIdentity() {
        return Long.valueOf(objectIdentity);
    }

    public Integer getParent_object() {
        return parent_object;
    }

    public void setParent_object(Integer parent_object) {
        this.parent_object = parent_object;
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
}
