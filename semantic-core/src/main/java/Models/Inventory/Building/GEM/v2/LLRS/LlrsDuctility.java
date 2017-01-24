package Models.Inventory.Building.GEM.v2.LLRS;

import Common.IDescription;

public enum LlrsDuctility implements IDescription {
    D99 ("D99", "Unknown ductility"),
    DUC ("DUC", "Ductile"),
    DNO ("DNO", "Non-ductile"),
    DBD ("DBD", "Has base isolation and/or energy dissipation device");

    private String code;
    private String description;
    private String longDescription;

    private LlrsDuctility(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private LlrsDuctility(String code, String description, String longDescription) {
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