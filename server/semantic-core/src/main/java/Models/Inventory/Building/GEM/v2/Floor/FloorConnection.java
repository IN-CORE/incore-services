package Models.Inventory.Building.GEM.v2.Floor;

import Common.IDescription;

public enum FloorConnection implements IDescription {
    FWC99 ("FWC99", "Unknown floor-wall diaphram connection"),
    FWCN ("FWCN", "Connection not provided"),
    FWCP ("FWCP", "Connection present");

    private String code;
    private String description;
    private String longDescription;

    private FloorConnection(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private FloorConnection(String code, String description, String longDescription) {
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