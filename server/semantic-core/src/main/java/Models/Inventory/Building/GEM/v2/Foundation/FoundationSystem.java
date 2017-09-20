package Models.Inventory.Building.GEM.v2.Foundation;

import Models.Inventory.Building.GEM.v2.Common.Qualifier;

public enum FoundationSystem implements Qualifier {
    FOS99 ("FOS99", "Unknown foundation system"),
    FOSSL ("FOSSL", "Shallow foundation, with lateral capacity"),
    FOSN ("FOSN", "Shallow foundation, no lateral capacity"),
    FOSDL ("FOSDL", "Deep foundation, with lateral capacity"),
    FOSDN ("FOSDN", "Deep foundation, no lateral capacity"),
    FOSO ("FOSO", "Other foundation");

    private String code;
    private String description;
    private String longDescription;

    private FoundationSystem(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private FoundationSystem(String code, String description, String longDescription) {
        this.code = code;
        this.description = description;
        this.longDescription = longDescription;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLongDescription() {
        return this.longDescription;
    }
}