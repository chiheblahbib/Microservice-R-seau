package picosoft.biz.arcep.client.kernel.model.acl.dto;

import java.io.Serializable;

public class StateWorkflowDTO implements Serializable {

    private Long id;

    private String name;

    private String description;

    private String label;

    private String color;
    private AclClassDTO classes;


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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public AclClassDTO getClasses() {
        return classes;
    }

    public void setClasses(AclClassDTO classes) {
        this.classes = classes;
    }
}
