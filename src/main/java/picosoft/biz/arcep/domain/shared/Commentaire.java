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
@Table(name = "commentaire", schema = "homologation")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@EntityListeners(AuditingEntityListener.class)
public class Commentaire extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "auteur", length = 100, nullable = true)
    private String auteur;

    @Column(name = "description", columnDefinition = "TEXT", nullable = true)
    private String description;

    @Column(name = "date_saisie", length = 25, nullable = true)
    private ZonedDateTime dateSaisie;

    @Column(name = "class_id", nullable = true)
    private Long classId;

    @Column(name = "object_id", nullable = true)
    private Long objectID;

    @Column(name = "externe", nullable = true)
    private Boolean externe;
}
