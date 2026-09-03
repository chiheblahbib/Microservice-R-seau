package picosoft.biz.arcep.domain.reseau.enumeration;

/**
 * Ce qui relie deux sites du reseau.
 *
 * Le formulaire demande cette nature liaison par liaison, avec la bande de
 * frequences, les debits et le nombre de bonds -- c'est ce qui permet de
 * verifier la coherence du reseau declare.
 */
public enum NatureLiaison {
    FIBRE_OPTIQUE,
    FH,
    BLR,
    VSAT,
    AUTRE
}
