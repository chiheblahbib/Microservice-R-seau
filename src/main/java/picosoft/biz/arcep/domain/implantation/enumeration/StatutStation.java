package picosoft.biz.arcep.domain.implantation.enumeration;

import java.util.stream.Stream;

public enum StatutStation {

    DRAFT("Brouillon"),
    CREATED("Déposé"),
    ENCOURS("En cours"),
    TREATED("Traité"),
    REJETE("Rejeté"),
    CANCELED("Annulé"),
    CLOSED("Clôturé");

    private final String label;

    StatutStation(String label) {
        this.label = label;
    }

    public static StatutStation of(String label) {
        return Stream.of(StatutStation.values())
                .filter(p -> p.getLabel().equals(label))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public String getLabel() {
        return label;
    }
}
