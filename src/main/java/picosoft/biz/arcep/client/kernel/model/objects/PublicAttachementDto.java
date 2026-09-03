package picosoft.biz.arcep.client.kernel.model.objects;

public class PublicAttachementDto {

    private String fileName;
    private byte[] decodedBytes;
    private Long classId;
    private Long objectId;
    private String reqFileDefName;

    private String objectData;

    public String getObjectData() {
        return objectData;
    }

    public void setObjectData(String objectData) {
        this.objectData = objectData;
    }

    public String getReqFileDefName() {
        return reqFileDefName;
    }

    public void setReqFileDefName(String reqFileDefName) {
        this.reqFileDefName = reqFileDefName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getDecodedBytes() {
        return decodedBytes;
    }

    public void setDecodedBytes(byte[] decodedBytes) {
        this.decodedBytes = decodedBytes;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }
}
