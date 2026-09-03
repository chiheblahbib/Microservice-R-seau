package picosoft.biz.arcep.client.kernel.model.acl;


import picosoft.biz.arcep.configuration.audit.Auditable;

import java.io.Serializable;

public class AclRuleSet extends Auditable implements Serializable {

    private Long id;

    private String name;

    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
