package picosoft.biz.arcep.domain.implantation.enumeration;

import java.util.stream.Stream;

public enum SensFrequence {

    EMISSION("Émission"),
    RECEPTION("Réception");

    private final String label;

    SensFrequence(String label) {
        this.label = label;
    }

    public static SensFrequence of(String label) {
        return Stream.of(SensFrequence.values())
                .filter(p -> p.getLabel().equals(label))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public String getLabel() {
        return label;
    }
}
