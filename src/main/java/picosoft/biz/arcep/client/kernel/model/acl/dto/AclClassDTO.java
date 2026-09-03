package picosoft.biz.arcep.client.kernel.model.acl.dto;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class AclClassDTO implements Serializable {

    private Long id;

    private String classe;

    private String simpleName;

    private String label;

    private String tableName;

    private StateAclWorkflowDTO defaultState;

    private ZonedDateTime sysdateCreated;

    private ZonedDateTime sysdateUpdated;

    private String syscreatedBy;

    private String sysupdatedBy;

    private String other;

  public String getOther() {
    return other;
  }

  public void setOther(String other) {
    this.other = other;
  }

  public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public StateAclWorkflowDTO getDefaultState() {
        return defaultState;
    }

    public void setDefaultState(StateAclWorkflowDTO defaultState) {
        this.defaultState = defaultState;
    }

    public ZonedDateTime getSysdateCreated() {
        return sysdateCreated;
    }

    public void setSysdateCreated(ZonedDateTime sysdateCreated) {
        this.sysdateCreated = sysdateCreated;
    }

    public ZonedDateTime getSysdateUpdated() {
        return sysdateUpdated;
    }

    public void setSysdateUpdated(ZonedDateTime sysdateUpdated) {
        this.sysdateUpdated = sysdateUpdated;
    }

    public String getSyscreatedBy() {
        return syscreatedBy;
    }

    public void setSyscreatedBy(String syscreatedBy) {
        this.syscreatedBy = syscreatedBy;
    }

    public String getSysupdatedBy() {
        return sysupdatedBy;
    }

    public void setSysupdatedBy(String sysupdatedBy) {
        this.sysupdatedBy = sysupdatedBy;
    }
}
