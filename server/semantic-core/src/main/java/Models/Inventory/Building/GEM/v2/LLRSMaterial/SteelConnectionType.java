package Models.Inventory.Building.GEM.v2.LLRSMaterial;

import Common.IDescription;

public enum SteelConnectionType implements IDescription {
    SC99 ("SC99", "Unknown connections"),
    WEL ("WEL", "Welded connections"),
    RIV ("RIV", "Riveted connections"),
    BOL ("BOL", "Bolted connections");

    private String code;
    private String description;
    private String longDescription;

    private SteelConnectionType(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private SteelConnectionType(String code, String description, String longDescription) {
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