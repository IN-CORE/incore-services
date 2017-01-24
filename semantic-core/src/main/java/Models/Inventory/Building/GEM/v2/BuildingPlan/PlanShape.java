package Models.Inventory.Building.GEM.v2.BuildingPlan;

import Models.Inventory.Building.GEM.v2.Common.Qualifier;

public enum PlanShape implements Qualifier {
    PLF99("PLF99", "Unknown plan shape"),
    PLFSQ("PLFSQ", "Square, solid"),
    PLFSQO("PLFSQO", "Square, with interior opening (e.g. a doughnut)"),
    PLFR("PLFR", "Rectangular, solid"),
    PLFRO("PLFRO", "Rectangular, with an opening"),
    PLFL("PLFL", "L-shape"),
    PLFA("PLFA", "A-shape"),
    PLFB("PLFB", "B-shape"),
    PLFC("PLFC", "Curved, solid (e.g. circular, elliptical, ovoid)"),
    PLFCO("PLFCO", "Circular, with an opening"),
    PLFD("PLFD", "Triangular shape, solid"),
    PLFDO("PLFDO", "Triangular shape, with an opening"),
    PLFE("PLFE", "E-shape"),
    PLFF("PLFF", "F-shape"),
    PLFH("PLFH", "H-shape"),
    PLFS("PLFS", "S-shape"),
    PLFT("PLFT", "T-shape"),
    PLFU("PLFU", "U-shape"),
    PLFX("PLFX", "X-shape"),
    PLFY("PLFY", "Y-shape"),
    PLFI("PLFI", "Irregular shape");

    private String code;
    private String description;
    private String longDescription;

    private PlanShape(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private PlanShape(String code, String description, String longDescription) {
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

    public String getTaxonomyString() {
        return this.code;
    }
}