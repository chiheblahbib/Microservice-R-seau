package picosoft.biz.arcep.domain.implantation.enumeration;

import java.util.stream.Stream;

public enum TypeTrafic {

    VOIX("Voix"),
    INTERNET("Internet"),
    DONNEES("Données"),
    AUTRE("Autre");

    private final String label;

    TypeTrafic(String label) {
        this.label = label;
    }

    public static TypeTrafic of(String label) {
        return Stream.of(TypeTrafic.values())
                .filter(p -> p.getLabel().equals(label))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public String getLabel() {
        return label;
    }
}
