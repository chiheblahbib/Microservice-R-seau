package picosoft.biz.arcep.domain.implantation.enumeration;

import java.util.stream.Stream;

public enum NatureImplantation {

    NOUVELLE("Nouvelle"),
    DELOCALISATION("Délocalisation"),
    MODIFICATION("Modification"),
    PARTAGE_INFRASTRUCTURE("Partage d'infrastructure");

    private final String label;

    NatureImplantation(String label) {
        this.label = label;
    }

    public static NatureImplantation of(String label) {
        return Stream.of(NatureImplantation.values())
                .filter(p -> p.getLabel().equals(label))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public String getLabel() {
        return label;
    }
}
