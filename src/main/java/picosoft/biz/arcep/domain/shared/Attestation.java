package picosoft.biz.arcep.domain.shared;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.aeronef.DemandeAeronef;
import picosoft.biz.arcep.domain.declaratif.DemandeDeclaratif;
import picosoft.biz.arcep.domain.installateur.DemandeInstallateur;
import picosoft.biz.arcep.domain.ispc.DemandeIspc;
import picosoft.biz.arcep.domain.mmsi.DemandeMmsi;
import picosoft.biz.arcep.domain.navire.DemandeNavire;
import picosoft.biz.arcep.domain.numerocourt.DemandeNumeroCourt;
import picosoft.biz.arcep.domain.numerocourturgence.DemandeNumeroCourtUrgence;
import picosoft.biz.arcep.domain.pq.DemandePq;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;
import picosoft.biz.arcep.domain.ussd.DemandeUssd;
import picosoft.biz.arcep.domain.implantation.Station;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "attestation", schema = "homologation")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class Attestation extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "reference")
    private String reference;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "nom_modele", length = 255)
    private String nomModele;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_aeronef_id", nullable = true)
    private DemandeAeronef demandeAeronef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_declaratif_id", nullable = true)
    private DemandeDeclaratif demandeDeclaratif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_installateur_id", nullable = true)
    private DemandeInstallateur demandeInstallateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_ispc_id", nullable = true)
    private DemandeIspc demandeIspc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_mmsi_id", nullable = true)
    private DemandeMmsi demandeMmsi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_navire_id", nullable = true)
    private DemandeNavire demandeNavire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_numerocourt_id", nullable = true)
    private DemandeNumeroCourt demandeNumeroCourt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_numerocourturgence_id", nullable = true)
    private DemandeNumeroCourtUrgence demandeNumeroCourtUrgence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_pq_id", nullable = true)
    private DemandePq demandePq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_reseau_id", nullable = true)
    private DemandeReseau demandeReseau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_ussd_id", nullable = true)
    private DemandeUssd demandeUssd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = true)
    private Station station;
}
