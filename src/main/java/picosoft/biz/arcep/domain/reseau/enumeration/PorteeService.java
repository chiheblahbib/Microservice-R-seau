package picosoft.biz.arcep.domain.reseau.enumeration;

/**
 * L'etendue geographique d'un service de telephonie.
 *
 * Nulle pour l'acces a Internet et la transmission de donnees, que le
 * formulaire ne decline pas.
 */
public enum PorteeService {
    LOCALE,
    NATIONALE,
    INTERNATIONALE
}
