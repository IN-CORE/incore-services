package Models.Inventory.Building.GEM.v2.StructuralIrregularlity;

import Common.IDescription;

public enum StructuralIrregularity implements IDescription {
    IR99 ("IR99", "Unknown structural irregularity"),
    IRRE ("IRRE", "Regular structure"),
    IRIR ("IRIR", "Irregular structure");

    private String code;
    private String description;
    private String longDescription;

    private StructuralIrregularity(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private StructuralIrregularity(String code, String description, String longDescription) {
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