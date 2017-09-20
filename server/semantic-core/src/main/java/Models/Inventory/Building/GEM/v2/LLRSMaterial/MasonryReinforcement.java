package Models.Inventory.Building.GEM.v2.LLRSMaterial;

import Common.IDescription;

public enum MasonryReinforcement implements IDescription {
    MR99 ("MR99", "Unknown reinforcement"),
    RS ("RS", "Steel-reinforced"),
    RW ("RW", "Wood-reinforced"),
    RB ("RB", "Bamboo-, cane- or rope-reinforced"),
    RCM ("RCM", "Fibre reinforcing mesh"),
    RCB ("RCB", "Reinforced concrete bands");

    private String code;
    private String description;
    private String longDescription;

    private MasonryReinforcement(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private MasonryReinforcement(String code, String description, String longDescription) {
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