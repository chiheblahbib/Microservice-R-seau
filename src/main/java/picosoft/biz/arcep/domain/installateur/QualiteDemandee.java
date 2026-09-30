package picosoft.biz.arcep.domain.installateur;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import picosoft.biz.arcep.domain.installateur.enumeration.TypeAutorisation;

import javax.persistence.*;
import java.io.Serializable;

/** Une qualite demandee, rubrique 1. Cumulable -- voir TypeAutorisation. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "qualite_demandee", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class QualiteDemandee implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "qualite")
    private TypeAutorisation qualite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_installateur_id")
    private DemandeInstallateur demandeInstallateur;
}
