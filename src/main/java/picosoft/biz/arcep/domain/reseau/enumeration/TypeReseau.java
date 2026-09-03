package picosoft.biz.arcep.domain.reseau.enumeration;

/**
 * Rubrique 6 : les types de reseaux a exploiter, plusieurs choix possibles.
 *
 * AUTRE s'accompagne d'une precision libre -- le formulaire reserve trois
 * lignes a cet effet, et la laisser vide rendrait la declaration inexploitable.
 *
 * PMR porte une consequence tarifaire : un reseau PMR SEUL coute 300 000 FCFA
 * de frais de dossier, tout autre assemblage 500 000. Voir RefTarif.
 */
public enum TypeReseau {
    FH,
    VSAT,
    STATION_TERRIENNE,
    FIBRE_OPTIQUE,
    IOT,
    BLR,
    PMR,
    AUTRE
}
