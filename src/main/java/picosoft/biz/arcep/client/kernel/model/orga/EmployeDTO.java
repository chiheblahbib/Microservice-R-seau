package picosoft.biz.arcep.client.kernel.model.orga;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A DTO for the {EmployeDTO} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class EmployeDTO implements Serializable {

    private Long id;

    private String nom;

    private String prenom;

    private String employeeid;


    private String adresseMail;

    private String telephone;

    private String poste;

    private ZonedDateTime dateEmbauche;


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

    public String getAdresseMail() {
        return adresseMail;
    }

    public void setAdresseMail(String adresseMail) {
        this.adresseMail = adresseMail;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getPoste() {
        return poste;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public ZonedDateTime getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(ZonedDateTime dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    public String getEmployeeid() {
        return employeeid;
    }

    public void setEmployeeid(String employeeid) {
        this.employeeid = employeeid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EmployeDTO)) {
            return false;
        }

        EmployeDTO employeDTO = (EmployeDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, employeDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "EmployeDTO{" +
            "id=" + getId() +
            ", nom='" + getNom() + "'" +
            ", prenom='" + getPrenom() + "'" +
            ", adresseMail='" + getAdresseMail() + "'" +
            ", telephone='" + getTelephone() + "'" +
            ", poste='" + getPoste() + "'" +
            ", dateEmbauche='" + getDateEmbauche() + "'" +
            "}";
    }
}
