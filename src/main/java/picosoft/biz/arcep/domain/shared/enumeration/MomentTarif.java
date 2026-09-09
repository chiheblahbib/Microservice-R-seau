package picosoft.biz.arcep.domain.shared.enumeration;

import java.util.stream.Stream;

/**
 * Moment auquel un tarif s'applique dans la vie du dossier.
 */
public enum MomentTarif {

    DEPOT("Au dépôt du dossier"),
    DELIVRANCE("À la délivrance de l'autorisation"),
    ANNUEL("Redevance annuelle par station");

    private final String label;

    MomentTarif(String label) {
        this.label = label;
    }

    public static MomentTarif of(String label) {
        return Stream.of(MomentTarif.values())
                .filter(p -> p.getLabel().equals(label))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public String getLabel() {
        return label;
    }
}
