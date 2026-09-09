package picosoft.biz.arcep.domain.mmsi.enumeration;

/**
 * Les cases cochables de la rubrique 3, toutes categories confondues.
 *
 * UNE SEULE ENUMERATION pour les quatre blocs, plutot qu'une par bloc : les
 * valeurs ne se recoupent pas -- un « navire de mer a besoins mondiaux » ne
 * peut pas etre coche dans le bloc des aeronefs -- et quatre enumerations
 * auraient impose quatre colonnes dont trois toujours nulles.
 *
 * La coherence entre `categorie` et la case cochee est verifiee a la
 * soumission, pas par le schema : une contrainte en base aurait refuse les
 * brouillons a moitie remplis, que le formulaire autorise.
 */
public enum TypeBesoinMmsi {

    // ---- A : stations de navire
    NAVIRE_BESOINS_LOCAUX,
    NAVIRE_BESOINS_REGIONAUX,
    NAVIRE_BESOINS_MONDIAUX,
    NAVIRE_APPEL_GROUPE,

    // ---- B : stations cotieres
    COTIERE_RADIOCOMMUNICATION,
    COTIERE_PORTUAIRE,
    COTIERE_PILOTAGE,
    COTIERE_APPEL_GROUPE,
    COTIERE_APPEL_GROUPE_ADMINISTRATION,

    // ---- C : aeronefs
    AERONEF_VOILURE_FIXE,
    AERONEF_HELICOPTERE,
    AERONEF_IDENTITE_GROUPE,
    AERONEF_IDENTITE_GROUPE_ADMINISTRATION,

    // ---- D : aides a la navigation
    ATON_PHYSIQUE_AIS,
    ATON_VIRTUELLE_AIS,
    ATIS
}
