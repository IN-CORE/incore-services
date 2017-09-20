package Models.Inventory.Building.GEM.v2.LLRS;

import Common.IDescription;

public enum LlrsQual implements IDescription {
    D99 ("D99", "Unknown or undetermined direction"),
    PF ("PF", "Direction is parallel to street"),
    OF ("OF", "Direction is perpendicular to street");

    private String code;
    private String description;
    private String longDescription;

    private LlrsQual(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private LlrsQual(String code, String description, String longDescription) {
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