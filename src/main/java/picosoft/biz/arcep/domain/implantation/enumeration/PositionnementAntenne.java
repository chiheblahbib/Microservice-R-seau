package picosoft.biz.arcep.domain.implantation.enumeration;

import java.util.stream.Stream;

public enum PositionnementAntenne {

    INTERIEURE("Intérieure"),
    EXTERIEURE("Extérieure");

    private final String label;

    PositionnementAntenne(String label) {
        this.label = label;
    }

    public static PositionnementAntenne of(String label) {
        return Stream.of(PositionnementAntenne.values())
                .filter(p -> p.getLabel().equals(label))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public String getLabel() {
        return label;
    }
}
