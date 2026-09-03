package picosoft.biz.arcep.client.kernel.model.objects;

import java.io.Serializable;

public class AttachementInputDTO implements Serializable {

    private String uuid;
    private String fileBase64 ;
    private String fileName ;
    private String fileContentType ;

    private String reqFileDefName ;
    private String reqFileDefLabel ;
    private String emplacement ;
    private String comments ;
    private String physicalLocation ;
    private String comment ;

    private  String docTitle;
    private  boolean original;
    private  boolean certifiedCopy;
    private  boolean unloadable;

    private  String action;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getReqFileDefLabel() {
        return reqFileDefLabel;
    }

    public void setReqFileDefLabel(String reqFileDefLabel) {
        this.reqFileDefLabel = reqFileDefLabel;
    }

    public String getFileBase64() {
        return fileBase64;
    }

    public void setFileBase64(String fileBase64) {
        this.fileBase64 = fileBase64;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPhysicalLocation() {
        return physicalLocation;
    }

    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileContentType() {
        return fileContentType;
    }

    public void setFileContentType(String fileContentType) {
        this.fileContentType = fileContentType;
    }

    public String getReqFileDefName() {
        return reqFileDefName;
    }

    public void setReqFileDefName(String reqFileDefName) {
        this.reqFileDefName = reqFileDefName;
    }

    public String getEmplacement() {
        return emplacement;
    }

    public void setEmplacement(String emplacement) {
        this.emplacement = emplacement;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean isOriginal() {
        return original;
    }

    public void setOriginal(boolean original) {
        this.original = original;
    }

    public boolean isCertifiedCopy() {
        return certifiedCopy;
    }

    public void setCertifiedCopy(boolean certifiedCopy) {
        this.certifiedCopy = certifiedCopy;
    }

    public boolean isUnloadable() {
        return unloadable;
    }

    public void setUnloadable(boolean unloadable) {
        this.unloadable = unloadable;
    }

    public String getDocTitle() {
        return docTitle;
    }

    public void setDocTitle(String docTitle) {
        this.docTitle = docTitle;
    }

    public String isAction() {
        return action;
    }
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
