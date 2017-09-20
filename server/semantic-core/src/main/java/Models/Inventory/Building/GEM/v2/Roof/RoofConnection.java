package Models.Inventory.Building.GEM.v2.Roof;

import Models.Inventory.Building.GEM.v2.Common.Qualifier;

public enum RoofConnection implements Qualifier {
    RWC99 ("RWC99", "Unknown roof-wall diaphragm connection"),
    RWCN ("RWCN", "Connection not provided"),
    RWCP ("RWCP", "Connection present"),
    RTD99 ("RTD99", "Roof tie-down unknown"),
    RTDN ("RTDN", "Roof tie-down not provided"),
    RTDP ("RTDP", "Roof tie-down present");

    private String code;
    private String description;
    private String longDescription;

    private RoofConnection(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private RoofConnection(String code, String description, String longDescription) {
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