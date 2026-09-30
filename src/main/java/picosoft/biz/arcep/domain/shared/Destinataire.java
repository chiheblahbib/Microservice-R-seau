package picosoft.biz.arcep.domain.shared;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "destinataire", schema = "homologation")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class Destinataire extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nom", length = 255)
    private String nom;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "telephone", length = 50)
    private String telephone;

    @Column(name = "societe", length = 255)
    private String societe;

    @Column(name = "nationalite", length = 10)
    private String nationalite;

    @Column(name = "adresse", length = 500)
    private String adresse;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_complement_id", unique = true)
    private DemandeComplement demandeComplement;
}
