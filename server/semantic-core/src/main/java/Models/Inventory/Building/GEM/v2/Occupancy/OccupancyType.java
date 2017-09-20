package Models.Inventory.Building.GEM.v2.Occupancy;

import Models.Inventory.Building.GEM.v2.Common.Qualifier;

public enum OccupancyType implements Qualifier {
    OC99 ("OC99", "Unknown occupancy"),
    RES ("RES", "Residential"),
    COM ("COM", "Commercial and public"),
    MIX ("MIX", "Mixed use"),
    IND ("IND", "Industrial"),
    AGR ("AGR", "Agriculture"),
    ASS ("ASS", "Assembly"),
    GOV ("GOV", "Government"),
    EDU ("EDU", "Education"),
    OCO ("OCO", "Other occupancy");

    private String code;
    private String description;
    private String longDescription;

    private OccupancyType(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private OccupancyType(String code, String description, String longDescription) {
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