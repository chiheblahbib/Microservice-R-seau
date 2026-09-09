package picosoft.biz.arcep.domain.numerocourt.enumeration;

/**
 * Rubrique 4 : la forme du numero demande.
 *
 * CLASSIQUE : 8XYZ, ou X, Y et Z sont des chiffres de 0 a 9.
 * GOLD      : 8X8X, ou X est un chiffre de 0 a 9 -- une forme repetee, plus
 *             facile a retenir, et facturee 10 millions par an contre 4.
 *
 * Le type COMMANDE LE TARIF : c'est la seule donnee du formulaire dont depend
 * le montant de la redevance annuelle.
 */
public enum TypeNumeroCourt {
    CLASSIQUE,
    GOLD
}
