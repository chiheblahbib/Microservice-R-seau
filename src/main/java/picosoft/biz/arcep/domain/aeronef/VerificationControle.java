package picosoft.biz.arcep.domain.aeronef;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Une ligne du tableau « Verifications - Controle », rubrique 7.
 *
 * REMPLIE PAR L'ARCEP, PAS PAR LE DEMANDEUR. C'est la trace des controles
 * effectues pendant l'instruction : l'ecran de depot ne doit pas la proposer,
 * et le circuit l'alimente au fil des etapes.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "verification_controle", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class VerificationControle extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "uuid", updatable = false)
    private UUID uuid;

    @Column(name = "date_controle")
    private LocalDate dateControle;

    @Column(name = "lieu")
    private String lieu;

    /** Le visa de l'agent : un nom ou un matricule, pas une signature. */
    @Column(name = "visa")
    private String visa;

    @Column(name = "observations")
    private String observations;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_aeronef_id")
    private DemandeAeronef demandeAeronef;

    @PrePersist
    public void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
    }
}
