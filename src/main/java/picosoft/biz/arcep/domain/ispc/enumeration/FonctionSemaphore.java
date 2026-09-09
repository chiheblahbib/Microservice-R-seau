package picosoft.biz.arcep.domain.ispc.enumeration;

/**
 * Rubrique 6 : la nature de l'utilisation dans le reseau.
 *
 * Le formulaire precise « plus d'une fonction peut s'appliquer » : ce sont
 * donc des cases cumulables, pas un choix unique -- d'ou la collection
 * FonctionPointSemaphore plutot qu'une colonne sur le dossier.
 *
 * Les sigles sont ceux de la signalisation SS7 et restent tels quels : les
 * developper (« Signal Transfer Point ») produirait des libelles que personne
 * dans le metier n'emploie.
 */
public enum FonctionSemaphore {
    STP,
    SSP,
    OMC,
    RELAIS_SCCP,
    SEP,
    SCP,
    LR,
    ISC,
    GMSC,
    AUTRE
}
