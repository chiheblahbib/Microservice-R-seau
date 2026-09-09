package picosoft.biz.arcep.domain.declaratif.enumeration;

/**
 * Rubrique 7 : la nature des services a valeur ajoutee fournis.
 *
 * Le formulaire en liste treize, avec une ligne libre pour chacune ou porter
 * le CALENDRIER de mise en service -- d'ou ServiceDeclare, qui porte le type
 * et sa date, plutot que treize colonnes booleennes.
 */
public enum TypeServiceDeclare {
    TELEPHONIE_FIXE,
    TELEPHONIE_MOBILE,
    APPELS_INTERNATIONAUX,
    TRANSIT_COLLECTE_TELEPHONIQUE,
    TRANSIT_IP,
    ACCES_FIXE_INTERNET,
    ACCES_MOBILE_INTERNET,
    MISE_A_DISPOSITION_CONTENU,
    TRANSMISSION_DONNEES,
    HEBERGEMENT_SVA,
    RADIOMESSAGERIE,
    RENSEIGNEMENTS_TELEPHONIQUES,
    AUTRE
}
