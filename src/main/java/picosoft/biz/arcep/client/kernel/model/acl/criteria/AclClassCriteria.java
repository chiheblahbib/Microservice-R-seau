package picosoft.biz.arcep.client.kernel.model.acl.criteria;



import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.*;

import java.io.Serializable;

public class AclClassCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;
    private StringFilter classe; //fullname
    private StringFilter simpleName;
    private StringFilter label;
    private StringFilter tableName;
    private IntegerFilter securiteLevel;
    private StringFilter anomalieDescription;
    private ZonedDateTimeFilter anomalieDate;
    private StringFilter exportFolderNameFM;
    private StringFilter fwProcess;
    private BooleanFilter cumulative;
    private StringFilter other;
    private ZonedDateTimeFilter sysdateCreated;
    private ZonedDateTimeFilter sysdateUpdated;
    private StringFilter syscreatedBy;
    private StringFilter sysupdatedBy;
    private LongFilter defaultStateId;

    private StringFilter defaultStateName;

    private StringFilter defaultStateDescription;

    private StringFilter defaultStateLabel;

    private StringFilter defaultStateColor;
    private LongFilter defaultStateClassId;
    public AclClassCriteria() {
    }

    public AclClassCriteria(LongFilter id, StringFilter classe, StringFilter simpleName, StringFilter label, StringFilter tableName) {
        this.id = id;
        this.classe = classe;
        this.simpleName = simpleName;
        this.label = label;
        this.tableName = tableName;
    }

    public AclClassCriteria(AclClassCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.classe = other.classe == null ? null : other.classe.copy();
        this.simpleName = other.simpleName == null ? null : other.simpleName.copy();
        this.tableName = other.tableName == null ? null : other.tableName.copy();
        this.label = other.label == null ? null : other.label.copy();
    }

    public LongFilter getId() {
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public StringFilter getClasse() {
        return classe;
    }

    public void setClasse(StringFilter classe) {
        this.classe = classe;
    }

    public StringFilter getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(StringFilter simpleName) {
        this.simpleName = simpleName;
    }

    public StringFilter getLabel() {
        return label;
    }

    public void setLabel(StringFilter label) {
        this.label = label;
    }

    public StringFilter getTableName() {
        return tableName;
    }

    public void setTableName(StringFilter tableName) {
        this.tableName = tableName;
    }

    public IntegerFilter getSecuriteLevel() {
        return securiteLevel;
    }

    public void setSecuriteLevel(IntegerFilter securiteLevel) {
        this.securiteLevel = securiteLevel;
    }

    public StringFilter getAnomalieDescription() {
        return anomalieDescription;
    }

    public ZonedDateTimeFilter getAnomalieDate() {
        return anomalieDate;
    }

    public void setAnomalieDate(ZonedDateTimeFilter anomalieDate) {
        this.anomalieDate = anomalieDate;
    }

    public StringFilter getExportFolderNameFM() {
        return exportFolderNameFM;
    }

    public ZonedDateTimeFilter getSysdateCreated() {
        return sysdateCreated;
    }

    public void setSysdateCreated(ZonedDateTimeFilter sysdateCreated) {
        this.sysdateCreated = sysdateCreated;
    }

    public ZonedDateTimeFilter getSysdateUpdated() {
        return sysdateUpdated;
    }

    public void setSysdateUpdated(ZonedDateTimeFilter sysdateUpdated) {
        this.sysdateUpdated = sysdateUpdated;
    }

    public StringFilter getSyscreatedBy() {
        return syscreatedBy;
    }

    public void setSyscreatedBy(StringFilter syscreatedBy) {
        this.syscreatedBy = syscreatedBy;
    }

    public StringFilter getSysupdatedBy() {
        return sysupdatedBy;
    }

    public void setSysupdatedBy(StringFilter sysupdatedBy) {
        this.sysupdatedBy = sysupdatedBy;
    }

    public void setExportFolderNameFM(StringFilter exportFolderNameFM) {
        this.exportFolderNameFM = exportFolderNameFM;
    }

    public void setAnomalieDescription(StringFilter anomalieDescription) {
        this.anomalieDescription = anomalieDescription;
    }

    public StringFilter getFwProcess() {
        return fwProcess;
    }

    public void setFwProcess(StringFilter fwProcess) {
        this.fwProcess = fwProcess;
    }

    public BooleanFilter getCumulative() {
        return cumulative;
    }

    public void setCumulative(BooleanFilter cumulative) {
        this.cumulative = cumulative;
    }

    public StringFilter getOther() {
        return other;
    }

    public void setOther(StringFilter other) {
        this.other = other;
    }

    public LongFilter getDefaultStateId() {
        return defaultStateId;
    }

    public void setDefaultStateId(LongFilter defaultStateId) {
        this.defaultStateId = defaultStateId;
    }

    public StringFilter getDefaultStateName() {
        return defaultStateName;
    }

    public void setDefaultStateName(StringFilter defaultStateName) {
        this.defaultStateName = defaultStateName;
    }

    public StringFilter getDefaultStateDescription() {
        return defaultStateDescription;
    }

    public void setDefaultStateDescription(StringFilter defaultStateDescription) {
        this.defaultStateDescription = defaultStateDescription;
    }

    public StringFilter getDefaultStateLabel() {
        return defaultStateLabel;
    }

    public void setDefaultStateLabel(StringFilter defaultStateLabel) {
        this.defaultStateLabel = defaultStateLabel;
    }

    public StringFilter getDefaultStateColor() {
        return defaultStateColor;
    }

    public void setDefaultStateColor(StringFilter defaultStateColor) {
        this.defaultStateColor = defaultStateColor;
    }

    public LongFilter getDefaultStateClassId() {
        return defaultStateClassId;
    }

    public void setDefaultStateClassId(LongFilter defaultStateClassId) {
        this.defaultStateClassId = defaultStateClassId;
    }

    @Override
    public Criteria copy() {
        return new AclClassCriteria(this);
    }
}

