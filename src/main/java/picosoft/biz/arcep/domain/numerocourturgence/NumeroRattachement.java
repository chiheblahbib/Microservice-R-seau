package picosoft.biz.arcep.domain.numerocourturgence;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;

/** Un numero long ou fixe auquel le numero d'urgence est rattache, rubrique 4. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "numero_rattachement_urgence", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class NumeroRattachement implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Chaine : un numero garde ses zeros de tete et ne se calcule pas. */
    @Column(name = "numero", length = 32)
    @Size(max = 32)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_numerocourturgence_id")
    private DemandeNumeroCourtUrgence demandeNumeroCourtUrgence;
}
