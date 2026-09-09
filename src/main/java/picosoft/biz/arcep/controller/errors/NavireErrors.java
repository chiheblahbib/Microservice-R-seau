package picosoft.biz.arcep.controller.errors;


/**
 * Les cles d'erreur rendues a l'appelant.
 *
 * Le prefixe designe le SERVICE qui repond : elles sont visibles dans le
 * corps de la reponse HTTP, et servent au front a traduire. Heritees de la
 * copie, elles annoncaient IMPLANTATION -- un dossier de reseau refuse
 * renvoyait donc une erreur au nom d'un autre service.
 */
public class NavireErrors {

    public static final String CLASS = "NAVIRE.ERROR.CLASS";

    public static final String ACL_CLASS_NOT_FOUND = "NAVIRE.ERROR.CLASS_NOT_FOUND";

    public static final String OBJECT_NOT_FOUND = "NAVIRE.ERROR.OBJECT_NOT_FOUND";
    public static final String OBJECT_NOT_AUTHORIZED = "NAVIRE.ERROR.OBJECT_NOT_AUTHORIZED";

    /** Une piece declaree obligatoire par le referentiel du kernel est absente. */
    public static final String PIECES_MANQUANTES = "NAVIRE.ERROR.PIECES_MANQUANTES";

    public static final String OBJECT_NOT_VALID = "NAVIRE.ERROR.OBJECT_NOT_VALID";

    /** Le dossier a un circuit demarre ou une autorisation delivree : suppression refusee. */
    /** Tri demande sur une propriete de collection : mal defini, refuse. */
    public static final String SORT_NOT_SUPPORTED = "NAVIRE.ERROR.SORT_NOT_SUPPORTED";

    public static final String OBJECT_ENGAGED = "NAVIRE.ERROR.OBJECT_ENGAGED";

}
