package picosoft.biz.arcep.client.kernel.model.acl.dto;

import picosoft.biz.arcep.client.kernel.model.acl.enumeration.OrganismeNature;
import picosoft.biz.arcep.client.kernel.model.acl.enumeration.OrganismeType;

import java.io.Serializable;

public class OrganismeDTO implements Serializable {
    private Long id;

    private String identifiant;

    private String nom;

    private String addressLine1;
    private String addressLine2 = "";
    private String companyName;
    private String city;
    private String state;
    private String postalCode;
    private String countryCode;
    private String countryName;
    private String personName;
    private String phoneNumber;
    private String phoneExtension;
    private String faxNumber;
    private String email;
    private String echelle;

    private String abbreviation;

    private String siteWeb;

    private String matriculeFiscale;

    private String secteurDactivite;

    private String courriel;

    private String langue;

    private OrganismeType organismeType;

    private OrganismeNature organismeNature;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneExtension() {
        return phoneExtension;
    }

    public void setPhoneExtension(String phoneExtension) {
        this.phoneExtension = phoneExtension;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEchelle() {
        return echelle;
    }

    public void setEchelle(String echelle) {
        this.echelle = echelle;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getSiteWeb() {
        return siteWeb;
    }

    public void setSiteWeb(String siteWeb) {
        this.siteWeb = siteWeb;
    }

    public String getMatriculeFiscale() {
        return matriculeFiscale;
    }

    public void setMatriculeFiscale(String matriculeFiscale) {
        this.matriculeFiscale = matriculeFiscale;
    }

    public String getSecteurDactivite() {
        return secteurDactivite;
    }

    public void setSecteurDactivite(String secteurDactivite) {
        this.secteurDactivite = secteurDactivite;
    }

    public String getCourriel() {
        return courriel;
    }

    public void setCourriel(String courriel) {
        this.courriel = courriel;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public OrganismeType getOrganismeType() {
        return organismeType;
    }

    public void setOrganismeType(OrganismeType organismeType) {
        this.organismeType = organismeType;
    }

    public OrganismeNature getOrganismeNature() {
        return organismeNature;
    }

    public void setOrganismeNature(OrganismeNature organismeNature) {
        this.organismeNature = organismeNature;
    }
}
