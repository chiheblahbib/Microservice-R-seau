package picosoft.biz.arcep.client.kernel.model.acl.dto;

import java.util.Collections;
import java.util.List;

/**
 * Représente un utilisateur Keycloak avec ses informations de base.
 * Cette classe est utilisée pour transporter des données depuis et vers le backend.
 */
public class KeycloakUserDTO {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private boolean enabled;
    private Boolean hasLicense;
    private String name;
    private String matricule;
    private List<String> groups;
    private List<String> roles;

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getHasLicense() {
        return hasLicense;
    }

    public void setHasLicense(Boolean hasLicense) {
        this.hasLicense = hasLicense;
    }

    public KeycloakUserDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public KeycloakUserDTO(String id, String username, String email, String firstName, String lastName, boolean enabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
    }

    public KeycloakUserDTO(String id, String username, String email, boolean enabled, String name) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.enabled = enabled;
        this.name = name;
    }
}