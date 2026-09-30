package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * DTO de résultat pour la création d'une réponse à une demande de complément.
 * Contient des informations sur la reprise du dossier ASI parent.
 */
@Getter
@Setter
@NoArgsConstructor
public class ReponseDemandeComplementResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // === Données de la réponse ===
    private Long id;
    private String reponse;
    private ZonedDateTime dateReponse;
    private String nomIntervenant;
    private Long demandeComplementId;
    private Long classId;

    // === Informations sur la reprise du dossier ASI ===
    private Boolean asiReprie = false;
    private String messageReprise;
    private Long asiId;
    private String asiProcessInstanceId;
    private ZonedDateTime ancienDueDate;
    private ZonedDateTime nouveauDueDate;
    private Long dureeSuspensionSecondes;
    private Long suspensionId;

    public ReponseDemandeComplementResultDTO(ReponseDemandeComplementDTO dto) {
        this.id = dto.getId();
        this.reponse = dto.getReponse();
        this.dateReponse = dto.getDateReponse();
        this.nomIntervenant = dto.getNomIntervenant();
        this.demandeComplementId = dto.getDemandeComplementId();
        this.classId = dto.getClassId();
    }

    /**
     * Marque le résultat comme une reprise réussie du dossier ASI
     */
    public void marquerAsiReprie(Long asiId, String processInstanceId,
                                  ZonedDateTime ancienDueDate, ZonedDateTime nouveauDueDate,
                                  Long dureeSuspensionSecondes, Long suspensionId) {
        this.asiReprie = true;
        this.asiId = asiId;
        this.asiProcessInstanceId = processInstanceId;
        this.ancienDueDate = ancienDueDate;
        this.nouveauDueDate = nouveauDueDate;
        this.dureeSuspensionSecondes = dureeSuspensionSecondes;
        this.suspensionId = suspensionId;

        long jours = dureeSuspensionSecondes != null ? dureeSuspensionSecondes / 86400 : 0;
        long heures = dureeSuspensionSecondes != null ? (dureeSuspensionSecondes % 86400) / 3600 : 0;
        long minutes = dureeSuspensionSecondes != null ? (dureeSuspensionSecondes % 3600) / 60 : 0;

        this.messageReprise = String.format(
            "Le dossier ASI (id=%d) a été repris avec succès. " +
            "Durée de suspension : %dj %dh %dmin. " +
            "Ancien dueDate : %s | Nouveau dueDate : %s",
            asiId, jours, heures, minutes,
            ancienDueDate != null ? ancienDueDate.toString() : "N/A",
            nouveauDueDate != null ? nouveauDueDate.toString() : "N/A"
        );
    }

    /**
     * Marque le résultat comme sans reprise (pas d'ASI lié ou pas de suspension)
     */
    public void marquerSansReprise(String raison) {
        this.asiReprie = false;
        this.messageReprise = raison;
    }
}
