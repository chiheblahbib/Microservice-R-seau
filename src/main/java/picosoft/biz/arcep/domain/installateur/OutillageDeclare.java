package picosoft.biz.arcep.domain.installateur;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import picosoft.biz.arcep.domain.installateur.enumeration.TypeOutillage;

import javax.persistence.*;
import java.io.Serializable;

/** Un instrument declare, rubrique 7-2. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "outillage_declare", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class OutillageDeclare implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_outillage")
    private TypeOutillage typeOutillage;

    /** La designation saisie, quand typeOutillage vaut AUTRE. */
    @Column(name = "designation")
    private String designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_installateur_id")
    private DemandeInstallateur demandeInstallateur;
}
