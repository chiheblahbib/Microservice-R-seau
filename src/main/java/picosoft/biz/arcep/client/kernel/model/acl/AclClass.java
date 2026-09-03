package picosoft.biz.arcep.client.kernel.model.acl;


import javax.persistence.*;
import picosoft.biz.arcep.configuration.audit.Auditable;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

import java.io.Serializable;
import java.time.ZonedDateTime;


@Entity
@Table(name = "acl_class", schema = "public")
@Audited
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

public class AclClass extends Auditable implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "category", length = 70)
    private String category;
    @Column(name = "class", unique = true, length = 70, nullable = false)
    private String classe; //fullname

    @Column(name = "simple_name", length = 70, nullable = false)
    private String simpleName;

    @Column(name = "label", length = 70)
    private String label;

    @Column(name = "table_name", length = 70, nullable = false)
    private String tableName;

    @Column(name = "module_package_name")
    private String modulePackageName;

    @Column(name = "fw_process", length = 128)
    private String fwProcess;

    @Column(name = "other", length = 10485760)
    private String other;

    @Column(name = "json_schema", length = 10485760)
    private String jsonSchema;

    @Column(name = "securite_level")
    private Integer securiteLevel = 0;

    private String anomalieDescription;
    private ZonedDateTime anomalieDate;
    @Column(name = "export_folder_NameFM", length = 254)
    private String exportFolderNameFM = "${(exportfoldername)!}";
    @OneToOne(fetch = FetchType.LAZY)
    private StateWorkflow defaultState;
    @Column(name = "read_form_name_fm", length = 2048)
    private String readFormNameFM; //
    @Column(name = "edit_form_namefm", length = 2048)
    private String editFormNameFM; //

    @Column(name = "sequence_name_fm", length = 128)
    private String sequenceNameFM;


    public Integer getSecuriteLevel() {
        return securiteLevel;
    }

    public void setSecuriteLevel(Integer securiteLevel) {
        this.securiteLevel = securiteLevel;
    }

    public Long getId() {
        return id;
    }

    public String getJsonSchema() {
        return jsonSchema;
    }

    public void setJsonSchema(String jsonSchema) {
        this.jsonSchema = jsonSchema;
    }

    public String getFwProcess() {
        return fwProcess;
    }

    public void setFwProcess(String fwProcess) {
        this.fwProcess = fwProcess;
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

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }


    public String getSequenceNameFM() {
        return sequenceNameFM;
    }

    public void setSequenceNameFM(String sequenceNameFM) {
        this.sequenceNameFM = sequenceNameFM;
    }


    public StateWorkflow getDefaultState() {
        return defaultState;
    }

    public void setDefaultState(StateWorkflow defaultState) {
        this.defaultState = defaultState;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getEditFormNameFM() {
        return editFormNameFM;
    }

    public void setEditFormNameFM(String editFormNameFM) {
        this.editFormNameFM = editFormNameFM;
    }

    public String getReadFormNameFM() {
        return readFormNameFM;
    }

    public void setReadFormNameFM(String readFormNameFM) {
        this.readFormNameFM = readFormNameFM;
    }
}

