package picosoft.biz.arcep.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/** Une ligne du tableau des equipements de bord, rubrique 5. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipementBordNavireDTO implements Serializable {

    private Long id;

    private String designation;

    private String marque;

    private String typeMateriel;

    /**
     * Chaines et non nombres : le formulaire ne fixe ni unite ni format.
     * Voir EquipementBord.
     */
    private String puissance;

    private String bandesFrequences;

    /** Colonne « AIS/ASN » du formulaire : texte libre, voir l'entite. */
    private String aisAsn;

    private Long demandeNavireId;
}
