package picosoft.biz.arcep.client.kernel.model.acl;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.envers.Audited;

import javax.persistence.*;


@Entity
@Table(name = "acl_sid", schema = "public")
@Audited
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

public class AclSid {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    @org.hibernate.annotations.Index(name = "principal_sid_index") // Adding an index
    private boolean principal = true;

    @Column(unique = true, nullable = false)
    @org.hibernate.annotations.Index(name = "sid_index") // Adding an index
    private String sid;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isPrincipal() {
        return principal;
    }

    public void setPrincipal(boolean principal) {
        this.principal = principal;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }
}
