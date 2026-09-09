package picosoft.biz.arcep.domain.aeronef.enumeration;

/**
 * Restriction de parcours declaree, rubrique 5 du formulaire.
 *
 * IFR : vol aux instruments. FR : le formulaire ecrit « Parcours FR », sans
 * developper -- il s'agit tres probablement de VFR (vol a vue), mais le
 * libelle est repris tel quel plutot que corrige : c'est au metier de
 * trancher, pas au code de deviner.
 */
public enum RestrictionParcours {

    IFR("Parcours IFR"),
    FR("Parcours FR");

    private final String label;

    RestrictionParcours(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
