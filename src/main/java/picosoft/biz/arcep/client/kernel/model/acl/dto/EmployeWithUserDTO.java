package picosoft.biz.arcep.client.kernel.model.acl.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * DTO combiné représentant un employé avec les informations
 * associées à son compte Keycloak (le cas échéant).
 */
@Data
@NoArgsConstructor
public class EmployeWithUserDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String fonction;
    private String matricule;
    private String keycloakId;
    private KeycloakUserDTO keycloakUser;
    public EmployeWithUserDTO(Long id, String nom, String prenom, String fonction, String matricule, String keycloakId, KeycloakUserDTO keycloakUser) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.fonction = fonction;
        this.matricule = matricule;
        this.keycloakId = keycloakId;
        this.keycloakUser = keycloakUser;
    }
    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getNom() {
        return nom;
    }
    public void setNom(String nom) {
        this.nom = nom;
    }
    public String getPrenom() {
        return prenom;
    }
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    public String getFonction() {
        return fonction;
    }
    public void setFonction(String fonction) {
        this.fonction = fonction;
    }
    public String getMatricule() {
        return matricule;
    }
    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }
    public String getKeycloakId() {
        return keycloakId;
    }
    public void setKeycloakId(String keycloakId) {
        this.keycloakId = keycloakId;
    }
    public KeycloakUserDTO getKeycloakUser() {
        return keycloakUser;
    }
    public void setKeycloakUser(KeycloakUserDTO keycloakUser) {
        this.keycloakUser = keycloakUser;
    }
}