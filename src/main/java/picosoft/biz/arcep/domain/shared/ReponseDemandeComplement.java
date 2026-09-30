package picosoft.biz.arcep.domain.shared;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "reponse_demande_complement", schema = "homologation")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class ReponseDemandeComplement extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Lob
    @Column(name = "reponse")
    private String reponse;

    @Column(name = "date_reponse")
    private ZonedDateTime dateReponse;

    @Column(name = "nom_intervenant", length = 255)
    private String nomIntervenant;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_complement_id", nullable = false, unique = true)
    private DemandeComplement demandeComplement;

    @Column(name = "class_id")
    private Long classId;
}
