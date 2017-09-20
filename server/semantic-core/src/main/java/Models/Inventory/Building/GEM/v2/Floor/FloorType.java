package Models.Inventory.Building.GEM.v2.Floor;

import Common.IDescription;

public enum FloorType implements IDescription {
    FM99 ("FM99", "Masonry, unknown"),
    FM1 ("FM1", "Vaulted masonry"),
    FM2 ("FM2", "Shallow-arched masonry"),
    FM3 ("FM3", "Composite cast-in-place RC and masonry floor system"),
    FE99 ("FE99", "Earthen, unknown"),
    FC99 ("FC99", "Concrete, unknown"),
    FC1 ("FC1", "Cast-in-place beamless RC floor"),
    FC2 ("FC2", "Cast-in-place beam-supported RC floor"),
    FC3 ("FC3", "Precast concrete floor system with RC topping"),
    FC4 ("FC4", "Precast concrete floor system without RC topping"),
    FME99 ("FME99", "Metal, unknown"),
    FME1 ("FME1", "Metal beams, trusses, or joists supporting light flooring"),
    FME2 ("FME2", "Metal beams supporting precast concrete slabs"),
    FME3 ("FME3", "Composite steel deck and concrete slab"),
    FW99 ("FW99", "Wood, unknown"),
    FW1 ("FW1", "Wooden beams or trusses and joists supporting light flooring"),
    FW2 ("FW2", "Wooden beams or trusses and joists supporting heavy flooring"),
    FW3 ("FW3", "Wood-based sheets on joists or beams"),
    FW4 ("FW4", "Plywood panels or other light-weight panels for floor");

    private String code;
    private String description;
    private String longDescription;

    private FloorType(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private FloorType(String code, String description, String longDescription) {
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