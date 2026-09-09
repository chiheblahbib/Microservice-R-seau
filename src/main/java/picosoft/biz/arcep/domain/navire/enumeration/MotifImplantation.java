package picosoft.biz.arcep.domain.navire.enumeration;

/**
 * Rubrique 5.1 : pourquoi la station est implantee la.
 *
 * Distincte de NatureDemande, qui porte sur le DOSSIER (nouvelle demande,
 * modification, renouvellement). Ici il s'agit de la STATION : un
 * renouvellement d'autorisation peut porter sur une station creee il y a
 * cinq ans, et une nouvelle demande sur une extension d'un site existant.
 * Les confondre ferait perdre l'une des deux informations.
 */
public enum MotifImplantation {
    CREATION,
    EXTENSION,
    MODIFICATION,
    AUTRE
}
