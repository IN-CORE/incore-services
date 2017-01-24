package Models.Inventory.Building.GEM.v2.Roof;

import Models.Inventory.Building.GEM.v2.Common.Qualifier;

public enum RoofCoverMaterial implements Qualifier {
    RMT99 ("RMT99", "Unknown roof covering"),
    RMN ("RMN", "Concrete roof without additional covering"),
    RMT1 ("RMT1", "Clay or concrete tile"),
    RMT2 ("RMT2", "Fibre cement or metal tile"),
    RMT3 ("RMT3", "Membrane roofing"),
    RMT4 ("RMT4", "Slate"),
    RMT5 ("RMT5", "Stone slab"),
    RMT6 ("RMT6", "Metal or asbestos sheets"),
    RMT7 ("RMT7", "Wooden and asphalt shingles"),
    RMT8 ("RMT8", "Vegetative"),
    RMT9 ("RMT9", "Earthen"),
    RMT10 ("RMT10", "Solar panelled roofs"),
    RMT11 ("RMT11", "Tensile membrane or fabric roof"),
    RMTO ("RMTO", "Roof covering, other");

    private String code;
    private String description;
    private String longDescription;

    private RoofCoverMaterial(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private RoofCoverMaterial(String code, String description, String longDescription) {
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