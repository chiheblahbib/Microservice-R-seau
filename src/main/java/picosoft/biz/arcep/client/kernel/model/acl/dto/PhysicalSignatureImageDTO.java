package picosoft.biz.arcep.client.kernel.model.acl.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class PhysicalSignatureImageDTO implements Serializable {
    private Long id;
    private String content;
    private String contentType;
}