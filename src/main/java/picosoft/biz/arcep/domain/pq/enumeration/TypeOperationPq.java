package picosoft.biz.arcep.domain.pq.enumeration;

/**
 * Rubrique 3, seconde question : « Il s'agit ».
 *
 * RESTITUTION est un sens de circulation inverse de l'attribution : l'operateur
 * REND des blocs qu'il n'utilise plus. Le meme formulaire sert aux deux, mais
 * l'instruction n'est evidemment pas la meme -- d'ou une enumeration plutot
 * qu'un booleen `estUneAttribution`, qui aurait fait lire la restitution comme
 * l'absence d'attribution.
 */
public enum TypeOperationPq {
    ATTRIBUTION,
    RESTITUTION
}
