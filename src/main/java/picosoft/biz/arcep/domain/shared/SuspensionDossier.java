package picosoft.biz.arcep.domain.shared;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import picosoft.biz.arcep.configuration.audit.Auditable;

import javax.persistence.*;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "suspension_dossier", schema = "homologation")
public class SuspensionDossier extends Auditable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type_dossier", length = 50, nullable = false)
    private String typeDossier;

    @Column(name = "dossier_id", nullable = false)
    private Long dossierId;

    @Column(name = "process_instance_id", length = 255, nullable = false)
    private String processInstanceId;

    @Column(name = "date_suspension", nullable = false)
    private ZonedDateTime dateSuspension;

    @Column(name = "date_reprise")
    private ZonedDateTime dateReprise;

    @Column(name = "duree_suspension_secondes")
    private Long dureeSuspensionSecondes;

    @Column(name = "due_date_avant_suspension")
    private ZonedDateTime dueDateAvantSuspension;

    @Column(name = "due_date_apres_reprise")
    private ZonedDateTime dueDateApresReprise;

    @Column(name = "motif", length = 255)
    private String motif;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(name = "created_date")
    private ZonedDateTime createdDate;

    @PrePersist
    public void prePersist() {
        if (createdDate == null) {
            createdDate = ZonedDateTime.now();
        }
    }
}
