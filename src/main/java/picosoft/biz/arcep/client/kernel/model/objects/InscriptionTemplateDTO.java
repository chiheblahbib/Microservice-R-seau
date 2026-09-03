package picosoft.biz.arcep.client.kernel.model.objects;


import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * A DTO for the {@link biz.picosoft.kernel.global.domain.Application} entity.
 */
public class InscriptionTemplateDTO implements Serializable {

    private Long id;

    private Boolean enabled = true;

    private String name;

    private String description;

    private Boolean redirect = false;

    private String redirectUrl;

    private String formeo;

    private ZonedDateTime sysdateCreated;

    private ZonedDateTime sysdateUpdated;

    private String syscreatedBy;

    private String sysupdatedBy;

    private String creationUserEventAlias;

    private Long creationUserEventId;

    private String creationAdminEventAlias;

    private Long creationAdminEventId;

    private String approveEventAlias;

    private Long approveEventId;

    private String refuseEventAlias;

    private Long refuseEventId;

    private String confirmEventAlias;

    private Long confirmEventId;

    private String sequenceName;

    private Long sequenceId;

    private String creationUserStateName;

    private Long creationUserStateId;

    private String creationAdminStateName;

    private Long creationAdminStateId;

    private String confirmStateName;

    private Long confirmStateId;

    private String approveStateName;

    private Long approveStateId;

    private String refuseStateName;

    private Long refuseStateId;

    private String rules;

    private Set<Profile> profiles = new HashSet<>();

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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRedirect() {
        return redirect;
    }

    public void setRedirect(Boolean redirect) {
        this.redirect = redirect;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public String getFormeo() {
        return formeo;
    }

    public void setFormeo(String formeo) {
        this.formeo = formeo;
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


    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public String getApproveEventAlias() {
        return approveEventAlias;
    }

    public void setApproveEventAlias(String approveEventAlias) {
        this.approveEventAlias = approveEventAlias;
    }

    public String getRefuseEventAlias() {
        return refuseEventAlias;
    }

    public void setRefuseEventAlias(String refuseEventAlias) {
        this.refuseEventAlias = refuseEventAlias;
    }

    public Long getApproveEventId() {
        return approveEventId;
    }

    public void setApproveEventId(Long approveEventId) {
        this.approveEventId = approveEventId;
    }

    public Long getRefuseEventId() {
        return refuseEventId;
    }

    public void setRefuseEventId(Long refuseEventId) {
        this.refuseEventId = refuseEventId;
    }

    public Long getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(Long sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getCreationUserEventAlias() {
        return creationUserEventAlias;
    }

    public void setCreationUserEventAlias(String creationUserEventAlias) {
        this.creationUserEventAlias = creationUserEventAlias;
    }

    public Long getCreationUserEventId() {
        return creationUserEventId;
    }

    public void setCreationUserEventId(Long creationUserEventId) {
        this.creationUserEventId = creationUserEventId;
    }

    public String getCreationAdminEventAlias() {
        return creationAdminEventAlias;
    }

    public void setCreationAdminEventAlias(String creationAdminEventAlias) {
        this.creationAdminEventAlias = creationAdminEventAlias;
    }

    public Long getCreationAdminEventId() {
        return creationAdminEventId;
    }

    public void setCreationAdminEventId(Long creationAdminEventId) {
        this.creationAdminEventId = creationAdminEventId;
    }

    public String getConfirmEventAlias() {
        return confirmEventAlias;
    }

    public void setConfirmEventAlias(String confirmEventAlias) {
        this.confirmEventAlias = confirmEventAlias;
    }

    public Long getConfirmEventId() {
        return confirmEventId;
    }

    public void setConfirmEventId(Long confirmEventId) {
        this.confirmEventId = confirmEventId;
    }

    public String getCreationUserStateName() {
        return creationUserStateName;
    }

    public void setCreationUserStateName(String creationUserStateName) {
        this.creationUserStateName = creationUserStateName;
    }

    public Long getCreationUserStateId() {
        return creationUserStateId;
    }

    public void setCreationUserStateId(Long creationUserStateId) {
        this.creationUserStateId = creationUserStateId;
    }

    public String getCreationAdminStateName() {
        return creationAdminStateName;
    }

    public void setCreationAdminStateName(String creationAdminStateName) {
        this.creationAdminStateName = creationAdminStateName;
    }

    public Long getCreationAdminStateId() {
        return creationAdminStateId;
    }

    public void setCreationAdminStateId(Long creationAdminStateId) {
        this.creationAdminStateId = creationAdminStateId;
    }

    public String getConfirmStateName() {
        return confirmStateName;
    }

    public void setConfirmStateName(String confirmStateName) {
        this.confirmStateName = confirmStateName;
    }

    public Long getConfirmStateId() {
        return confirmStateId;
    }

    public void setConfirmStateId(Long confirmStateId) {
        this.confirmStateId = confirmStateId;
    }

    public String getApproveStateName() {
        return approveStateName;
    }

    public void setApproveStateName(String approveStateName) {
        this.approveStateName = approveStateName;
    }

    public Long getApproveStateId() {
        return approveStateId;
    }

    public void setApproveStateId(Long approveStateId) {
        this.approveStateId = approveStateId;
    }

    public String getRefuseStateName() {
        return refuseStateName;
    }

    public void setRefuseStateName(String refuseStateName) {
        this.refuseStateName = refuseStateName;
    }

    public Long getRefuseStateId() {
        return refuseStateId;
    }

    public void setRefuseStateId(Long refuseStateId) {
        this.refuseStateId = refuseStateId;
    }

    public Set<Profile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Set<Profile> profiles) {
        this.profiles = profiles;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InscriptionTemplateDTO)) {
            return false;
        }

        return id != null && id.equals(((InscriptionTemplateDTO) o).id);
    }

}
