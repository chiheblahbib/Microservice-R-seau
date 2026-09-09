package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;
import java.io.Serializable;

/** Une ligne du tableau des equipements de bord, rubrique 5. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipementBordAeronefDTO implements Serializable {

    private Long id;

    @Size(max = 150)
    private String designation;

    @Size(max = 100)
    private String marque;

    @Size(max = 100)
    private String typeMateriel;

    /**
     * Chaines et non nombres : le formulaire ne fixe ni unite ni format.
     * Voir EquipementBord.
     */
    @Size(max = 50)
    private String puissance;

    @Size(max = 150)
    private String bandesFrequences;

    private Long demandeAeronefId;
}
