package Models.Inventory.Building.GEM.v2.StructuralIrregularlity;

import Common.IDescription;

public enum StructuralHorizIrreg implements IDescription {
    IRN ("IRN", "No irregularity"),
    TOR ("TOR", "Torsion eccentricity"),
    REC ("REC", "Re-entrant corner"),
    IRHO ("IRHO", "Other horizontal irregularity");

    private String code;
    private String description;
    private String longDescription;

    private StructuralHorizIrreg(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private StructuralHorizIrreg(String code, String description, String longDescription) {
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