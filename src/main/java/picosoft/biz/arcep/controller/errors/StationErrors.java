package picosoft.biz.arcep.controller.errors;


public class StationErrors {

    public static final String CLASS = "STATION.ERROR.CLASS";

    public static final String ACL_CLASS_NOT_FOUND = "STATION.ERROR.CLASS_NOT_FOUND";

    public static final String OBJECT_NOT_FOUND = "STATION.ERROR.OBJECT_NOT_FOUND";
    public static final String OBJECT_NOT_AUTHORIZED = "STATION.ERROR.OBJECT_NOT_AUTHORIZED";

    public static final String OBJECT_NOT_VALID = "STATION.ERROR.OBJECT_NOT_VALID";

    /** Le dossier a un circuit demarre ou une autorisation delivree : suppression refusee. */
    /** Tri demande sur une propriete de collection : mal defini, refuse. */
    public static final String SORT_NOT_SUPPORTED = "STATION.ERROR.SORT_NOT_SUPPORTED";

    public static final String OBJECT_ENGAGED = "STATION.ERROR.OBJECT_ENGAGED";

}
