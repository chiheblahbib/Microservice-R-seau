package picosoft.biz.arcep.domain.implantation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.implantation.enumeration.*;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "frequence", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class Frequence extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "sens", length = 16)
    private SensFrequence sens;

    @Column(name = "frequence_centrale_mhz")
    private Double frequenceCentraleMhz;

    @Column(name = "bande_mhz")
    private Double bandeMhz;

    @Column(name = "zone_service_km")
    private Double zoneServiceKm;

    /** Puissance apparente rayonnee, en watts. */
    @Column(name = "par_watts")
    private Double parWatts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;
}
