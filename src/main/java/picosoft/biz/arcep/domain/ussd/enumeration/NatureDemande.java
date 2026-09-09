package picosoft.biz.arcep.domain.ussd.enumeration;

/**
 * Rubrique 5 du formulaire.
 *
 * RENOUVELLEMENT n'est pas equivalent aux deux autres : le formulaire exige
 * alors une copie de l'ancienne autorisation. Le controle de soumission s'y
 * appuie.
 */
public enum NatureDemande {
    NOUVEAU,
    MODIFICATION,
    RENOUVELLEMENT
}
