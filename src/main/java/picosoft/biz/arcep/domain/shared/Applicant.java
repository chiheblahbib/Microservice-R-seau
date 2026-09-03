package picosoft.biz.arcep.domain.shared;


import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.domain.shared.enumeration.ApplicantType;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "applicant", schema = "homologation")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class Applicant extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicant_type", nullable = true)
    private ApplicantType applicantType;

    @Column(name = "qualification", length = 100, nullable = true)
    private String qualification;

    @Column(name = "applicant_name", length = 100, nullable = true)
    private String applicantName;

    @Column(name = "company", length = 100)
    private String company;

    @Column(name = "trade_register_number", length = 50, nullable = true)
    private String tradeRegisterNumber;

    @Column(name = "nationality", length = 50, nullable = true)
    private String nationality;

    @Column(name = "nationality_complet", length = 100, nullable = true)
    private String nationalityComplet;

    @Column(name = "address", length = 200, nullable = true)
    private String address;

    @Column(name = "phone", length = 20, nullable = true)
    private String phone;

    @Column(name = "fax", length = 20)
    private String fax;

    @Column(name = "email", length = 100, nullable = true)
    private String email;

    @Column(name = "website", length = 100)
    private String website;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "demande_reseau_id",
            nullable = true,
            unique = true
    )
    private DemandeReseau demandeReseau;
}
