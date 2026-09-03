package picosoft.biz.arcep.client.kernel.model.acl.dto;

import picosoft.biz.arcep.client.kernel.model.acl.TypeSID;

import java.io.Serializable;

public class AclSidDTO implements Serializable {

    private Long id;
    private boolean principal;
    private String sid;
    private TypeSID type;
    private Object entity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public TypeSID getType() {
        return type;
    }

    public void setType(TypeSID type) {
        this.type = type;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }
}
