package Models.Inventory.Building.GEM.v2.LLRSMaterial;

import Common.IDescription;

public enum MasonaryMortarType implements IDescription {
    MO99 ("MO99", "Unknown mortar type"),
    MON ("MON", "No mortar"),
    MOM ("MOM", "Mud mortar"),
    MOL ("MOL", "Lime mortar"),
    MOC ("MOC", "Cement mortar"),
    MOCL ("MOCL", "Cement:lime mortar", "Cement lime mortar"),
    SP99 ("SP99", "Unknown stone type"),
    SPLI ("SPLI", "Limestone"),
    SPSA ("SPSA", "Sandstone"),
    SPTU ("SPTU", "Tuff"),
    SPSL ("SPSL", "Slate"),
    SPGR ("SPGR", "Granite"),
    SPBA ("SPBA", "Basalt"),
    SPO ("SPO", "Stone, other type");

    private String code;
    private String description;
    private String longDescription;

    private MasonaryMortarType(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private MasonaryMortarType(String code, String description, String longDescription) {
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