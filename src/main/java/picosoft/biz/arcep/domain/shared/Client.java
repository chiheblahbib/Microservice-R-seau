package picosoft.biz.arcep.domain.shared;


import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.domain.shared.enumeration.ClientType;
import picosoft.biz.arcep.domain.shared.enumeration.ModeTransport;
import picosoft.biz.arcep.domain.shared.enumeration.UsageEquipement;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "client", schema = "homologation")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class Client extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", nullable = true)
    private ClientType clientType;

    @Column(name = "qualification", length = 100, nullable = true)
    private String qualification;

    @Column(name = "client_name", length = 100, nullable = true)
    private String clientName;

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

    @Column(name = "nature_activite", length = 500, nullable = true)
    private String natureActivite;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_equipement", length = 16, nullable = true)
    private UsageEquipement usageEquipement;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_transport", length = 16, nullable = true)
    private ModeTransport modeTransport;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "demande_reseau_id",
            nullable = true,
            unique = true
    )
    private DemandeReseau demandeReseau;
}
