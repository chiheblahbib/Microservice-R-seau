package picosoft.biz.arcep.domain.implantation.enumeration;

import java.util.stream.Stream;

public enum NatureSupport {

    CHATEAU_EAU("Château d'eau"),
    IMMEUBLE("Immeuble"),
    PYLONE("Pylône"),
    MAT_SUR_PYLONE("Mât sur pylône"),
    AUTRE("Autre");

    private final String label;

    NatureSupport(String label) {
        this.label = label;
    }

    public static NatureSupport of(String label) {
        return Stream.of(NatureSupport.values())
                .filter(p -> p.getLabel().equals(label))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public String getLabel() {
        return label;
    }
}
