package Models.Inventory.Building.GEM.v2.LLRSMaterial;

import Common.IDescription;

public enum MaterialTechnology implements IDescription {
    CT99 ("CT99", "Concrete, unknown"),
    CIP ("CIP", "Cast-in-place concrete"),
    PC ("PC", "Precast concrete"),
    CIPPS ("CIPPS", "Cast-in-place prestressed concrete"),
    PCPS ("PCPS", "Precast prestressed concrete"),
    S99 ("S99", "Steel, unknown"),
    SL ("SL", "Cold-formed steel members"),
    SR ("SR", "Hot-rolled steel members"),
    SO ("SO", "Steel, other"),
    ME99 ("ME99", "Metal, unknown"),
    MEIR ("MEIR", "Iron"),
    MEO ("MEO", "Metal, other"),
    MUN99 ("MUN99", "Masonry unit, unknown"),
    ADO ("ADO", "Adobe blocks"),
    ST99 ("ST99", "Stone, unknown technology"),
    STRUB ("STRUB", "Rubble (field stone) or semi-dressed stone"),
    STDRE ("STDRE", "Dressed stone"),
    CL99 ("CL99", "Fired clay unit, unknown type"),
    CLBRS ("CLBRS", "Fired clay solid bricks"),
    CLBRH ("CLBRH", "Fired clay hollow bricks"),
    CLBLH ("CLBLH", "Fired clay hollow blocks or tiles"),
    CB99 ("CB99", "Concrete blocks, unknown type"),
    CBS ("CBS", "Concrete blocks, solid"),
    CBH ("CBH", "Concrete blocks, hollow"),
    MO ("MO", "Masonry unit, other"),
    ET99 ("ET99", "Earth technology, unknown"),
    ETR ("ETR", "Rammed earth"),
    ETC ("ETC", "Cob or wet construction"),
    ETO ("ETO", "Earth technology, other"),
    W99 ("W99", "Wood, unknown"),
    WHE ("WHE", "Heavy wood"),
    WLI ("WLI", "Light wood members"),
    WS ("WS", "Solid wood"),
    WWD ("WWD", "Wattle and daub"),
    WBB ("WBB", "Bamboo"),
    WO ("WO", "Wood, other");

    private String code;
    private String description;
    private String longDescription;

    private MaterialTechnology(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private MaterialTechnology(String code, String description, String longDescription) {
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