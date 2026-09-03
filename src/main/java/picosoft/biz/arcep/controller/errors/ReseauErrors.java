package picosoft.biz.arcep.controller.errors;


/**
 * Les cles d'erreur rendues a l'appelant.
 *
 * Le prefixe designe le SERVICE qui repond : elles sont visibles dans le
 * corps de la reponse HTTP, et servent au front a traduire. Heritees de la
 * copie, elles annoncaient IMPLANTATION -- un dossier de reseau refuse
 * renvoyait donc une erreur au nom d'un autre service.
 */
public class ReseauErrors {

    public static final String CLASS = "RESEAU.ERROR.CLASS";

    public static final String ACL_CLASS_NOT_FOUND = "RESEAU.ERROR.CLASS_NOT_FOUND";

    public static final String OBJECT_NOT_FOUND = "RESEAU.ERROR.OBJECT_NOT_FOUND";
    public static final String OBJECT_NOT_AUTHORIZED = "RESEAU.ERROR.OBJECT_NOT_AUTHORIZED";

    /** Une piece declaree obligatoire par le referentiel du kernel est absente. */
    public static final String PIECES_MANQUANTES = "RESEAU.ERROR.PIECES_MANQUANTES";

    public static final String OBJECT_NOT_VALID = "RESEAU.ERROR.OBJECT_NOT_VALID";

    /** Le dossier a un circuit demarre ou une autorisation delivree : suppression refusee. */
    /** Tri demande sur une propriete de collection : mal defini, refuse. */
    public static final String SORT_NOT_SUPPORTED = "RESEAU.ERROR.SORT_NOT_SUPPORTED";

    public static final String OBJECT_ENGAGED = "RESEAU.ERROR.OBJECT_ENGAGED";

}
