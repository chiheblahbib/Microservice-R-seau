package picosoft.biz.arcep.domain.shared;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import picosoft.biz.arcep.configuration.audit.Auditable;
import picosoft.biz.arcep.domain.reseau.DemandeReseau;

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
    @JoinColumn(
            name = "demande_reseau_id",
            nullable = true
    )
    private DemandeReseau demandeReseau;
}
