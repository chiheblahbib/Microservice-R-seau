package picosoft.biz.arcep.domain.installateur.enumeration;

/**
 * Rubrique 1 : la nature de la demande.
 *
 * CUMULABLES : le formulaire presente quatre cases a cocher, et l'annexe
 * tarifaire parle explicitement d'« Installateur ET/OU Distributeur ». Un
 * dossier peut donc porter deux qualites a la fois -- d'ou une collection,
 * et non une colonne unique.
 *
 * RENOUVELLEMENT est dans la meme liste sur le papier, mais n'est pas une
 * qualite : c'est ce que le dossier FAIT, pas ce qu'il demande a etre. Il est
 * donc porte a part, par `natureDemande`, comme partout ailleurs dans le
 * module.
 */
public enum TypeAutorisation {
    INSTALLATEUR,
    DISTRIBUTEUR,
    SOUS_DISTRIBUTEUR
}
