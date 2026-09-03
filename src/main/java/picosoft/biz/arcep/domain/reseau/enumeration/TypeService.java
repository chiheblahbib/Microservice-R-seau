package picosoft.biz.arcep.domain.reseau.enumeration;

/**
 * Rubrique 7 : les services a exploiter, plusieurs choix possibles.
 *
 * Les deux formes de telephonie se declinent en portee -- locale, nationale,
 * internationale -- ce que porte PorteeService. Les deux autres n'en ont pas.
 */
public enum TypeService {
    ACCES_INTERNET,
    TRANSMISSION_DONNEES,
    TELEPHONIE,
    TELEPHONIE_IP
}
