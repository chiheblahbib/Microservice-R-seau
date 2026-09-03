package picosoft.biz.arcep.client.kernel.model.global;


import picosoft.biz.arcep.client.kernel.model.objects.AttachementDTO;

import java.util.List;

public class AclClassFilesDto {
    private List<GetRequestFileDefinitionDTO> requestFileDefinition;
    private List<AttachementDTO> attachement;
    private String MandatoryTemplateFileName;
    private String OptionalTemplateFileName;
    private String officeTemplateFileName;
    private String emailTemplateFileName;
    private List<String> DefaultTemplateFileName;


    public String getOfficeTemplateFileName() {
        return officeTemplateFileName;
    }

    public void setOfficeTemplateFileName(String officeTemplateFileName) {
        this.officeTemplateFileName = officeTemplateFileName;
    }

    public String getEmailTemplateFileName() {
        return emailTemplateFileName;
    }

    public void setEmailTemplateFileName(String emailTemplateFileName) {
        this.emailTemplateFileName = emailTemplateFileName;
    }

    public String getMandatoryTemplateFileName() {
        return MandatoryTemplateFileName;
    }


    public void setMandatoryTemplateFileName(String mandatoryTemplateFileName) {
        MandatoryTemplateFileName = mandatoryTemplateFileName;
    }

    public String getOptionalTemplateFileName() {
        return OptionalTemplateFileName;
    }

    public void setOptionalTemplateFileName(String optionalTemplateFileName) {
        OptionalTemplateFileName = optionalTemplateFileName;
    }

    public List<String> getDefaultTemplateFileName() {
        return DefaultTemplateFileName;
    }

    public void setDefaultTemplateFileName(List<String> defaultTemplateFileName) {
        DefaultTemplateFileName = defaultTemplateFileName;
    }

    public List<GetRequestFileDefinitionDTO> getRequestFileDefinition() {
        return requestFileDefinition;
    }

    public void setRequestFileDefinition(List<GetRequestFileDefinitionDTO> requestFileDefinition) {
        this.requestFileDefinition = requestFileDefinition;
    }

    public List<AttachementDTO> getAttachement() {
        return attachement;
    }

    public void setAttachement(List<AttachementDTO> attachement) {
        this.attachement = attachement;
    }

    @Override
    public String toString() {
        return "AclClassFilesDto{" +
                "requestFileDefinition=" + requestFileDefinition +
                ", attachement=" + attachement +
                '}';
    }
}
