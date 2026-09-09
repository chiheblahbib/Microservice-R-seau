package picosoft.biz.arcep.domain.declaratif.enumeration;

/**
 * Rubrique 6 : l'etendue geographique.
 *
 * PROVINCE laisse une liste de provinces a cote ; NATIONAL couvre tout le
 * territoire. Les deux s'excluent -- declarer « tout le territoire » ET trois
 * provinces ne voudrait rien dire.
 */
public enum TypeCouverture {
    PROVINCE,
    NATIONAL
}
