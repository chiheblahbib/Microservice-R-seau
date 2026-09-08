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
 * Un type de reseau declare a la rubrique 6. Plusieurs par dossier.
 *
 * La precision n'est renseignee que pour AUTRE : le formulaire y reserve trois
 * lignes, et une case cochee sans texte n'apprend rien a l'instruction.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "type_reseau_declare", schema = "drrrs")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class TypeReseauDeclare extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 24)
    private TypeReseau type;

    @Column(name = "precision_autre", length = 255)
    @Size(max = 255)
    private String precisionAutre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demande_reseau_id")
    private DemandeReseau demandeReseau;
}
