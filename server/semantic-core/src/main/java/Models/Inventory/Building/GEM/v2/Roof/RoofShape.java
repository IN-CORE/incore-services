package Models.Inventory.Building.GEM.v2.Roof;

import Models.Inventory.Building.GEM.v2.Common.Qualifier;

public enum RoofShape implements Qualifier {
    RSH99 ("RSH99", "Unknown roof shape"),
    RSH1 ("RSH1", "Flat"),
    RSH2 ("RSH2", "Pitched with gable ends"),
    RSH3 ("RSH3", "Pitched and hipped"),
    RSH4 ("RSH4", "Pitched with dormers"),
    RSH5 ("RSH5", "Monopitch"),
    RSH6 ("RSH6", "Sawtooth"),
    RSH7 ("RSH7", "Curved"),
    RSH8 ("RSH8", "Complex regular"),
    RSH9 ("RSH9", "Complex irregular"),
    RSHO ("RSHO", "Roof shape, other");

    private String code;
    private String description;
    private String longDescription;

    private RoofShape(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private RoofShape(String code, String description, String longDescription) {
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