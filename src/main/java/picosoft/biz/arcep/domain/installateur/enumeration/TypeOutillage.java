package picosoft.biz.arcep.domain.installateur.enumeration;

/**
 * Rubrique 7-2 : l'outillage professionnel.
 *
 * Onze instruments nommes, plus une ligne libre repetee (« Autres materiels
 * 1, 2, 3, etc. »). AUTRE couvre cette derniere, et l'entite porte alors la
 * designation saisie.
 */
public enum TypeOutillage {
    FREQUENCEMETRE,
    WATTMETRE,
    TOSMETRE,
    GENERATEUR_BF,
    GENERATEUR_HF,
    MULTIMETRE,
    OSCILLOSCOPE,
    ANALYSEUR,
    BANC_DE_MESURES,
    RADIOTELEPHONE,
    AUTRE
}
