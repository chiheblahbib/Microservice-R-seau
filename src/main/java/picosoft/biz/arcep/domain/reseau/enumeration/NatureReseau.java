package picosoft.biz.arcep.domain.reseau.enumeration;

/**
 * Rubrique 4 du formulaire.
 *
 * Ce n'est pas une simple etiquette : un reseau OUVERT_AU_PUBLIC declenche
 * l'exigence de six pieces supplementaires -- capital, actionnaires,
 * repartition du capital, plan de deploiement, caracteristiques du systeme
 * d'information, niveaux de qualite de service -- que le formulaire marque
 * d'un asterisque. C'est aussi ce qui distingue les deux regimes
 * d'instruction.
 */
public enum NatureReseau {
    PRIVE,
    OUVERT_AU_PUBLIC
}
