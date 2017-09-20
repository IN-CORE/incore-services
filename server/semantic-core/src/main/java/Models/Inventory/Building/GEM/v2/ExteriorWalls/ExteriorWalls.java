package Models.Inventory.Building.GEM.v2.ExteriorWalls;

import Models.Inventory.Building.GEM.v2.Common.Qualifier;

public enum ExteriorWalls implements Qualifier {
    EW99 ("EW99", "Unknown material"),
    EWC ("EWC", "Concrete"),
    EWG ("EWG", "Glass"),
    EWE ("EWE", "Earth"),
    EWMA ("EWMA", "Masonry"),
    EWME ("EWME", "Metal"),
    EWV ("EWV", "Vegetative"),
    EWW ("EWW", "Wood"),
    EWSL ("EWSL", "Stucco finish on light framing"),
    EWPL ("EWPL", "Plastic/vinyl, various"),
    EWCB ("EWCB", "Cement-based boards"),
    EWO ("EWO", "Other material");

    private String code;
    private String description;
    private String longDescription;

    private ExteriorWalls(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private ExteriorWalls(String code, String description, String longDescription) {
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