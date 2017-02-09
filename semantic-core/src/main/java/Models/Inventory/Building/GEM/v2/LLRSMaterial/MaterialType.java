package Models.Inventory.Building.GEM.v2.LLRSMaterial;

import Common.IDescription;

public enum MaterialType implements IDescription {
    MAT99 ("MAT99", "Unknown material"),
    C99 ("C99", "Concrete, unknown reinforcement"),
    CU ("CU", "Concrete,unreinforced"),
    CR ("CR", "Concrete, reinforced"),
    SRC ("SRC", "Concrete, composite with steel section"),
    S ("S", "Steel"),
    ME ("ME", "Metal (except steel)"),
    M99 ("M99", "Masonry, unknown reinforcement"),
    MUR ("MUR", "Masonry, unreinforced"),
    MCF ("MCF", "Masonry, confined"),
    MR ("MR", "Masonry, reinforced"),
    E99 ("E99", "Earth, unknown reinforcement"),
    EU ("EU", "Earth, unreinforced"),
    ER ("ER", "Earth, reinforced"),
    W ("W", "Wood"),
    MATO ("MATO", "Other material");

    private String code;
    private String description;
    private String longDescription;

    private MaterialType(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private MaterialType(String code, String description, String longDescription) {
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