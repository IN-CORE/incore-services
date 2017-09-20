package Models.Inventory.Building.GEM.v2.BuildingPosition;

import Models.Inventory.Building.GEM.v2.Common.Qualifier;

public enum Position implements Qualifier {
    BP99("BP99", "Unknown building position"),
    BPD("BPD", "Detached building"),
    BP1("BP1", "Adjoining building(s) on one side"),
    BP2("BP2", "Adjoining buildings on two sides"),
    BP3("BP3", "Adjoining buildings on three sides");

    private String code;
    private String description;
    private String longDescription;

    private Position(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private Position(String code, String description, String longDescription) {
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