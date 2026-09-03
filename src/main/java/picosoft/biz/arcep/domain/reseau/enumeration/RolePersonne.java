package picosoft.biz.arcep.domain.reseau.enumeration;

/**
 * Les deux personnes physiques que le formulaire distingue.
 *
 * REQUERANT depose la demande ; RESPONSABLE repond du reseau une fois
 * exploite. Ce peut etre la meme personne, mais le formulaire les separe et
 * l'instruction a besoin de savoir laquelle est laquelle.
 */
public enum RolePersonne {
    REQUERANT,
    RESPONSABLE
}
