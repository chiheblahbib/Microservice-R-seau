package picosoft.biz.arcep.domain.declaratif;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import picosoft.biz.arcep.domain.declaratif.enumeration.TypeInfrastructure;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;

/** Une caracteristique de reseau declaree, rubrique 8.a. Cumulable. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "infrastructure_declaree", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class InfrastructureDeclaree implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_infrastructure", length = 40)
    private TypeInfrastructure typeInfrastructure;

    /** Le detail que le formulaire fait porter en regard de chaque ligne. */
    @Column(name = "precision_detail", length = 255)
    @Size(max = 255)
    private String precisionDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_declaratif_id")
    private DemandeDeclaratif demandeDeclaratif;
}
