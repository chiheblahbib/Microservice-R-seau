package picosoft.biz.arcep.domain.implantation.enumeration;

import java.util.stream.Stream;

public enum TypeAntenne {

    YAGI("Yagi"),
    CIERGE("Cierge"),
    DIPOLE("Dipôle"),
    TROMBONE("Trombone"),
    PARABOLE("Parabole"),
    AUTRE("Autre");

    private final String label;

    TypeAntenne(String label) {
        this.label = label;
    }

    public static TypeAntenne of(String label) {
        return Stream.of(TypeAntenne.values())
                .filter(p -> p.getLabel().equals(label))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public String getLabel() {
        return label;
    }
}
