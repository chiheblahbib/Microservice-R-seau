package picosoft.biz.arcep.domain.reseau;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.reseau.enumeration.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * Un service declare a la rubrique 7. Plusieurs par dossier.
 *
 * La portee -- locale, nationale, internationale -- ne concerne que les deux
 * formes de telephonie. Elle reste nulle pour l'acces a Internet et la
 * transmission de donnees, que le formulaire ne decline pas.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "service_declare", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class ServiceDeclare extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 32)
    private TypeService type;

    @Enumerated(EnumType.STRING)
    @Column(name = "portee", length = 16)
    private PorteeService portee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_reseau_id")
    private DemandeReseau demandeReseau;
}
