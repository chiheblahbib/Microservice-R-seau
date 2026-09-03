package picosoft.biz.arcep.client.kernel.model.objects;

import picosoft.biz.arcep.client.kernel.model.acl.AclClass;
import picosoft.biz.arcep.client.kernel.model.global.StateWorkflow;
import picosoft.biz.arcep.configuration.audit.Auditable;

import java.io.Serializable;
import java.time.ZonedDateTime;


public class ObjectState extends Auditable implements Serializable {


    private Long id;
    private AclClass businessClass;

    private StateWorkflow currentState;

    private Long objectId;

    private ZonedDateTime dateUpdateState;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AclClass getBusinessClass() {
        return businessClass;
    }

    public void setBusinessClass(AclClass businessClass) {
        this.businessClass = businessClass;
    }

    public StateWorkflow getCurrentState() {
        return currentState;
    }


    public void setCurrentState(StateWorkflow currentState) {
        this.currentState = currentState;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public ZonedDateTime getDateUpdateState() {
        return dateUpdateState;
    }

    public void setDateUpdateState(ZonedDateTime dateUpdateState) {
        this.dateUpdateState = dateUpdateState;
    }
}
