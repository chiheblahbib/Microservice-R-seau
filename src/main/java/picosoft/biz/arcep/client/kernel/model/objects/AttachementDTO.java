package picosoft.biz.arcep.client.kernel.model.objects;


import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 *
 */
public class AttachementDTO implements Serializable {

    private Long id;

    private String absctract;

    private String anomalieDescription;

    private LocalDate anomalieDate;

    private String checksum;

    private String fileName;

    private String fileType;

    private Boolean locked;

    private Boolean transferable;

    private Integer securiteLevel;

    private String docSize;

    private byte[] thumbnail;

    private Integer docCopies;

    private LocalDate docExpirationDate;

    private LocalDate docIssueDate;

    private String docId;

    private String issueAdress;

    private Boolean isReport;

    private String docTitle;

    private Long objectId;

    private Long classId;

    private FilesTypeDTO filesTypeDTO;

    private String responsableSid;


    private ZonedDateTime sysdateCreated;

    private ZonedDateTime sysdateUpdated;

    private String syscreatedBy;

    private String sysupdatedBy;

    private GetRequestFileDefinitionDTO requestFileDefinition;


    public AttachementDTO(Long id, String checksum, String fileName, String fileType, Boolean locked, Boolean transferable, Integer securiteLevel, Boolean isReport, String docTitle, Long objectId, Long classId) {
        this.id = id;
        this.checksum = checksum;
        this.fileName = fileName;
        this.fileType = fileType;
        this.locked = locked;
        this.transferable = transferable;
        this.securiteLevel = securiteLevel;
        this.isReport = isReport;
        this.docTitle = docTitle;
        this.objectId = objectId;
        this.classId = classId;
    }

    public AttachementDTO() {
    }

    public GetRequestFileDefinitionDTO getRequestFileDefinition() {
        return requestFileDefinition;
    }

    public void setRequestFileDefinition(GetRequestFileDefinitionDTO requestFileDefinition) {
        this.requestFileDefinition = requestFileDefinition;
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

    public String getResponsableSid() {
        return responsableSid;
    }

    public void setResponsableSid(String responsableSid) {
        this.responsableSid = responsableSid;
    }

    public FilesTypeDTO getFilesTypeDTO() {
        return filesTypeDTO;
    }

    public void setFilesTypeDTO(FilesTypeDTO filesTypeDTO) {
        this.filesTypeDTO = filesTypeDTO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAbsctract() {
        return absctract;
    }

    public void setAbsctract(String absctract) {
        this.absctract = absctract;
    }

    public String getAnomalieDescription() {
        return anomalieDescription;
    }

    public void setAnomalieDescription(String anomalieDescription) {
        this.anomalieDescription = anomalieDescription;
    }

    public LocalDate getAnomalieDate() {
        return anomalieDate;
    }

    public void setAnomalieDate(LocalDate anomalieDate) {
        this.anomalieDate = anomalieDate;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public Boolean getTransferable() {
        return transferable;
    }

    public void setTransferable(Boolean transferable) {
        this.transferable = transferable;
    }

    public Integer getSecuriteLevel() {
        return securiteLevel;
    }

    public void setSecuriteLevel(Integer securiteLevel) {
        this.securiteLevel = securiteLevel;
    }

    public String getDocSize() {
        return docSize;
    }

    public void setDocSize(String docSize) {
        this.docSize = docSize;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Boolean getReport() {
        return isReport;
    }

    public void setReport(Boolean report) {
        isReport = report;
    }

    public Integer getDocCopies() {
        return docCopies;
    }

    public void setDocCopies(Integer docCopies) {
        this.docCopies = docCopies;
    }

    public LocalDate getDocExpirationDate() {
        return docExpirationDate;
    }

    public void setDocExpirationDate(LocalDate docExpirationDate) {
        this.docExpirationDate = docExpirationDate;
    }

    public LocalDate getDocIssueDate() {
        return docIssueDate;
    }

    public void setDocIssueDate(LocalDate docIssueDate) {
        this.docIssueDate = docIssueDate;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getIssueAdress() {
        return issueAdress;
    }

    public void setIssueAdress(String issueAdress) {
        this.issueAdress = issueAdress;
    }

    public Boolean getIsReport() {
        return isReport;
    }

    public void setIsReport(Boolean isReport) {
        this.isReport = isReport;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AttachementDTO)) {
            return false;
        }

        AttachementDTO attachementDTO = (AttachementDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, attachementDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AttachementDTO{" +
                "id=" + getId() +
                ", absctract='" + getAbsctract() + "'" +
                ", anomalieDescription='" + getAnomalieDescription() + "'" +
                ", anomalieDate='" + getAnomalieDate() + "'" +
                ", checksum='" + getChecksum() + "'" +
                ", fileName='" + getFileName() + "'" +
                ", fileType='" + getFileType() + "'" +
                ", locked='" + getLocked() + "'" +
                ", transferable='" + getTransferable() + "'" +
                ", securiteLevel=" + getSecuriteLevel() +
                ", docSize='" + getDocSize() + "'" +
                ", thumbnail='" + getThumbnail() + "'" +
                ", docCopies=" + getDocCopies() +
                ", docExpirationDate='" + getDocExpirationDate() + "'" +
                ", docIssueDate='" + getDocIssueDate() + "'" +
                ", docId='" + getDocId() + "'" +
                ", issueAdress='" + getIssueAdress() + "'" +
                ", isReport='" + getIsReport() + "'" +
                ", docTitle='" + getDocTitle() + "'" +
                ", objectId=" + getObjectId() +
                ", classId=" + getClassId() +
                "}";
    }
}
