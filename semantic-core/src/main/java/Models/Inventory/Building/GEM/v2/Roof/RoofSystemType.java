package Models.Inventory.Building.GEM.v2.Roof;

import Models.Inventory.Building.GEM.v2.Common.Qualifier;

public enum RoofSystemType implements Qualifier {
    RM99 ("RM99", "Masonry, unknown"),
    RM1 ("RM1", "Vaulted masonry"),
    RM2 ("RM2", "Shallow-arched masonry"),
    RM3 ("RM3", "Composite masonry and concrete roof system"),
    RE99 ("RE99", "Earthen, unknown"),
    RE1 ("RE1", "Vaulted earthen roof"),
    RC99 ("RC99", "Concrete, unknown"),
    RC1 ("RC1", "Cast-in-place beamless RC roof"),
    RC2 ("RC2", "Cast-in-place beam-supported reinforced concrete roof"),
    RC3 ("RC3", "Precast concrete roof with RC topping"),
    RC4 ("RC4", "Precast concrete roof system without RC topping"),
    RME99 ("RME99", "Metal, unknown"),
    RME1 ("RME1", "Metal beams or trusses supporting light roofing"),
    RME2 ("RME2", "Metal beams supporting precast concrete slabs"),
    RME3 ("RME3", "Composite steel deck and concrete slab"),
    RWO99 ("RWO99", "Wood, unknown"),
    RWO1 ("RWO1", "Wooden structure with light roof covering"),
    RWO2 ("RWO2", "Wooden beams or trusses with heavy roof covering"),
    RWO3 ("RWO3", "Wood-based sheets on rafters or purlins"),
    RWO4 ("RWO4", "Plywood panels or other light-weight panels for roof"),
    RWO5 ("RWO5", "Bamboo, straw or thatch roof"),
    RFA1 ("RFA1", "Inflatable or tensile membrane roof"),
    RFAO ("RFAO", "Fabric, other");

    private String code;
    private String description;
    private String longDescription;

    private RoofSystemType(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private RoofSystemType(String code, String description, String longDescription) {
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