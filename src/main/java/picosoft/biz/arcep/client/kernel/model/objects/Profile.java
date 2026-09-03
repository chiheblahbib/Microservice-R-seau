package picosoft.biz.arcep.client.kernel.model.objects;



import javax.persistence.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * A Profile.
 */
public class Profile implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Column(name = "description", length = 30)
    private String description;

    @Column(name = "comment", length = 50)
    private String comment;

    @Column(name = "mail", length = 50)
    private String mail;

    @Column(name = "orders")
    private int orders;

    @Lob
    @Column(name = "signature", length = 4096)
    private byte[] signature;

    @Column(name = "signature_content_type", length = 30)
    private String signatureContentType;

    /*FOR SecuriteLevel*/
    @Column(name = "securite_level", nullable = false)
    private Integer securiteLevel = 0;


    public String getSignatureContentType() {
        return signatureContentType;
    }

    public void setSignatureContentType(String signatureContentType) {
        this.signatureContentType = signatureContentType;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public Integer getSecuriteLevel() {
        return securiteLevel;
    }

    public void setSecuriteLevel(Integer securiteLevel) {
        this.securiteLevel = securiteLevel;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Profile comment(String comment) {
        this.comment = comment;
        return this;
    }

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }


    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Profile)) {
            return false;
        }
        return id != null && id.equals(((Profile) o).id);
    }

    @Override
      public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", comment='" + comment + '\'' +
                ", mail='" + mail + '\'' +
                ", orders=" + orders +
                ", signature=" + Arrays.toString(signature) +
                ", signatureContentType='" + signatureContentType + '\'' +
                ", securiteLevel=" + securiteLevel +
                '}';
    }
}
