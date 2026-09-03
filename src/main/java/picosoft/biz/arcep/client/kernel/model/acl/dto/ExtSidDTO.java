package picosoft.biz.arcep.client.kernel.model.acl.dto;

import java.util.List;

public class ExtSidDTO {
    private AclSidDTO sid;
    private List<AclSidDTO> ext;

    public AclSidDTO getSid() {
        return sid;
    }

    public void setSid(AclSidDTO sid) {
        this.sid = sid;
    }

    public List<AclSidDTO> getExt() {
        return ext;
    }

    public void setExt(List<AclSidDTO> ext) {
        this.ext = ext;
    }
}
