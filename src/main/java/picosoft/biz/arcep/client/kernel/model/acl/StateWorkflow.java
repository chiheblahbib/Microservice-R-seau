package picosoft.biz.arcep.client.kernel.model.acl;



import picosoft.biz.arcep.configuration.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.io.Serializable;
@Entity
@Table(name = "k_e_pa_states", uniqueConstraints = {@UniqueConstraint(columnNames = {"classes_id", "name"})}, schema = "kernel")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
@Audited
public class StateWorkflow extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 30)
    private String name;

    @Column(name = "description",length = 50)
    private String description;

    @Column(name = "label",length = 60)
    private String label;

    @Column(name = "color", length = 20)
    private String color;
    @Column(name = "is_end_state")
    private Boolean isEndState;

    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @ManyToOne(fetch = FetchType.LAZY)
    @org.hibernate.annotations.Index(name = "classWF_id_index") // Adding an index
    @JsonIgnore
    private AclClass classes;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Boolean getEndState() {
        return isEndState;
    }

    public void setEndState(Boolean endState) {
        isEndState = endState;
    }


    public AclClass getClasses() {
        return classes;
    }

    public void setClasses(AclClass classes) {
        this.classes = classes;
    }

    // jhipster-needle-entity-add-field - JHipster will add fields here
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

    public StateWorkflow name(String name) {
        this.name = name;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StateWorkflow description(String description) {
        this.description = description;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public StateWorkflow label(String label) {
        this.label = label;
        return this;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public StateWorkflow color(String color) {
        this.color = color;
        return this;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof StateWorkflow)) {
            return false;
        }
        return id != null && id.equals(((StateWorkflow) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    // prettier-ignore


    @Override
    public String toString() {
        return "StateWorkflow{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", label='" + label + '\'' +
                ", color='" + color + '\'' +
                ", classes=" + classes +
                '}';
    }
}
