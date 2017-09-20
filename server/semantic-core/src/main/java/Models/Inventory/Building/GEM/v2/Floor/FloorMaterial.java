package Models.Inventory.Building.GEM.v2.Floor;

import Common.IDescription;

public enum FloorMaterial implements IDescription {
    FN ("FN", "No elevated/suspended floor material"),
    F99 ("F99", "Unknown floor material"),
    FM ("FM", "Masonry"),
    FE ("FE", "Earthen"),
    FC ("FC", "Concrete"),
    FME ("FME", "Metal"),
    FW ("FW", "Wood"),
    FO ("FO", "Other floor material");

    private String code;
    private String description;
    private String longDescription;

    private FloorMaterial(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private FloorMaterial(String code, String description, String longDescription) {
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