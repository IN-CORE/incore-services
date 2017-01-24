package Models.Inventory.Building.GEM.v2.StructuralIrregularlity;

import Common.IDescription;

public enum StructuralVertIrreg implements IDescription {
    IRN ("IRN", "No irregularity"),
    SOS ("SOS", "Soft storey"),
    CRW ("CRW", "Cripple wall"),
    SHC ("SHC", "Short column"),
    POP ("POP", "Pounding potential"),
    SET ("SET", "Setback"),
    CHV ("CHV", "Change in vertical structure"),
    IRVO ("IRVO", "Other vertical irregularity");

    private String code;
    private String description;
    private String longDescription;

    private StructuralVertIrreg(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private StructuralVertIrreg(String code, String description, String longDescription) {
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