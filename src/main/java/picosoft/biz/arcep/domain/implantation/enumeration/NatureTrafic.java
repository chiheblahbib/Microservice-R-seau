package picosoft.biz.arcep.domain.implantation.enumeration;

import java.util.stream.Stream;

public enum NatureTrafic {

    NATIONAL("National"),
    INTERNATIONAL("International");

    private final String label;

    NatureTrafic(String label) {
        this.label = label;
    }

    public static NatureTrafic of(String label) {
        return Stream.of(NatureTrafic.values())
                .filter(p -> p.getLabel().equals(label))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public String getLabel() {
        return label;
    }
}
