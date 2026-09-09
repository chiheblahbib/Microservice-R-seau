package picosoft.biz.arcep.domain.ussd.enumeration;

/**
 * Rubrique 3 : la nature de l'activite.
 *
 * Une case de plus que le numero court : SERVICE_VALEUR_AJOUTEE. C'est
 * coherent -- un code USSD sert typiquement a un service a valeur ajoutee
 * (paiement mobile, consultation de solde), pas seulement a un reseau.
 */
public enum NatureActivite {
    TELEPHONIE_MOBILE,
    TELEPHONIE_FIXE,
    SERVICE_VALEUR_AJOUTEE,
    AUTRE
}
