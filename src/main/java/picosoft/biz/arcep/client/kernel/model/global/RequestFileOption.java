package picosoft.biz.arcep.client.kernel.model.global;

public enum RequestFileOption {

    Notapplicable("Non applicable"), OPTIONAL("Optionel"), MANDATORY("Obligatoire");

    private final String label;

    RequestFileOption(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
