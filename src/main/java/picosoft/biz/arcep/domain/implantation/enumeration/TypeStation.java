package picosoft.biz.arcep.domain.implantation.enumeration;

import java.util.stream.Stream;

public enum TypeStation {

    FIXE_SATELLITE("Fixe par satellite"),
    FAISCEAU_HERTZIEN("Faisceau hertzien"),
    STATION_BASE("Station de base"),
    RADIODIFFUSION("Radiodiffusion"),
    AUTRE("Autre");

    private final String label;

    TypeStation(String label) {
        this.label = label;
    }

    public static TypeStation of(String label) {
        return Stream.of(TypeStation.values())
                .filter(p -> p.getLabel().equals(label))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public String getLabel() {
        return label;
    }
}
