package picosoft.biz.arcep.domain.shared;


import javax.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.aeronef.DemandeAeronef;
import picosoft.biz.arcep.domain.declaratif.DemandeDeclaratif;
import picosoft.biz.arcep.domain.implantation.DemandeImplantation;
import picosoft.biz.arcep.domain.installateur.DemandeInstallateur;
import picosoft.biz.arcep.domain.ispc.DemandeIspc;
import picosoft.biz.arcep.domain.mmsi.DemandeMmsi;
import picosoft.biz.arcep.domain.navire.DemandeNavire;
import picosoft.biz.arcep.domain.numerocourt.DemandeNumeroCourt;
import picosoft.biz.arcep.domain.numerocourturgence.DemandeNumeroCourtUrgence;
import picosoft.biz.arcep.domain.pq.DemandePq;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.domain.ussd.DemandeUssd;
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
    @JoinColumn(name = "demande_aeronef_id", nullable = true, unique = true)
    private DemandeAeronef demandeAeronef;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_declaratif_id", nullable = true, unique = true)
    private DemandeDeclaratif demandeDeclaratif;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_implantation_id", nullable = true, unique = true)
    private DemandeImplantation demandeImplantation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_installateur_id", nullable = true, unique = true)
    private DemandeInstallateur demandeInstallateur;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_ispc_id", nullable = true, unique = true)
    private DemandeIspc demandeIspc;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_mmsi_id", nullable = true, unique = true)
    private DemandeMmsi demandeMmsi;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_navire_id", nullable = true, unique = true)
    private DemandeNavire demandeNavire;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_numerocourt_id", nullable = true, unique = true)
    private DemandeNumeroCourt demandeNumeroCourt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_numerocourturgence_id", nullable = true, unique = true)
    private DemandeNumeroCourtUrgence demandeNumeroCourtUrgence;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_pq_id", nullable = true, unique = true)
    private DemandePq demandePq;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_reseau_id", nullable = true, unique = true)
    private DemandeReseau demandeReseau;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_ussd_id", nullable = true, unique = true)
    private DemandeUssd demandeUssd;
}
