package picosoft.biz.arcep.controller.errors;


public class ImplantationErrors {

    public static final String CLASS = "IMPLANTATION.ERROR.CLASS";

    public static final String ACL_CLASS_NOT_FOUND = "IMPLANTATION.ERROR.CLASS_NOT_FOUND";

    public static final String OBJECT_NOT_FOUND = "IMPLANTATION.ERROR.OBJECT_NOT_FOUND";
    public static final String OBJECT_NOT_AUTHORIZED = "IMPLANTATION.ERROR.OBJECT_NOT_AUTHORIZED";

    /** Une piece declaree obligatoire par le referentiel du kernel est absente. */
    public static final String PIECES_MANQUANTES = "IMPLANTATION.ERROR.PIECES_MANQUANTES";

    public static final String OBJECT_NOT_VALID = "IMPLANTATION.ERROR.OBJECT_NOT_VALID";

    /** Le dossier a un circuit demarre ou une autorisation delivree : suppression refusee. */
    /** Tri demande sur une propriete de collection : mal defini, refuse. */
    public static final String SORT_NOT_SUPPORTED = "IMPLANTATION.ERROR.SORT_NOT_SUPPORTED";

    public static final String OBJECT_ENGAGED = "IMPLANTATION.ERROR.OBJECT_ENGAGED";

}
