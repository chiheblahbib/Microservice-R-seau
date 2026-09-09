package picosoft.biz.arcep.domain.mmsi.enumeration;

/**
 * Rubrique 3 : les quatre familles de stations, A a D.
 *
 * Le formulaire est organise en QUATRE BLOCS mutuellement exclusifs -- on
 * demande un MMSI pour un navire, OU pour une station cotiere, OU pour un
 * aeronef, OU pour une aide a la navigation. Chaque bloc a ses propres cases.
 *
 * La categorie est portee separement des cases : sans elle, deux dossiers
 * cochant « appel simultane d'un groupe » seraient indistinguables, alors que
 * l'un parle de navires et l'autre de stations cotieres.
 */
public enum CategorieStation {
    /** A -- stations de navire. */
    NAVIRE,
    /** B -- stations cotieres. */
    COTIERE,
    /** C -- aeronefs utilisant des identites du service mobile maritime. */
    AERONEF,
    /** D -- stations equipees d'un systeme d'identification automatique. */
    AIDE_NAVIGATION
}
