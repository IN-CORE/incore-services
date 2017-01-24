package Models.Inventory.Building.GEM.v2.Roof;

import Models.Inventory.Building.GEM.v2.Common.Qualifier;

public enum RoofSystemMaterial implements Qualifier {
    R99 ("R99", "Unknown roof material"),
    RM ("RM", "Masonry"),
    RE ("RE", "Earthen"),
    RC ("RC", "Concrete"),
    RME ("RME", "Metal"),
    RWO ("RWO", "Wood"),
    RFA ("RFA", "Fabric"),
    RO ("RO", "Other roof material");

    private String code;
    private String description;
    private String longDescription;

    private RoofSystemMaterial(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private RoofSystemMaterial(String code, String description, String longDescription) {
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