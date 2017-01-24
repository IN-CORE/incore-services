package Models.Inventory.Building.GEM.v2.LLRS;

import Common.IDescription;

public enum Llrs implements IDescription {
    L99 ("L99", "Unknown LLRS"),
    LN ("LN", "No LLRS"),
    LFM ("LFM", "Moment frame"),
    LFINF ("LFINF", "Infilled frame"),
    LFBR ("LFBR", "Braced frame"),
    LPB ("LPB", "Post and beam"),
    LWAL ("LWAL", "Wall"),
    LDUAL ("LDUAL", "Dual frame-wall system"),
    LFLS ("LFLS", "Flat slab/plate or waffle slab"),
    LFLSINF ("LFLSINF", "Infilled flat slab/plate or infilled waffle slab"),
    LH ("LH", "Hybrid LLRS"),
    LO ("LO", "Other LLRS");

    private String code;
    private String description;
    private String longDescription;

    private Llrs(String code, String description) {
        this.code = code;
        this.description = description;
        this.longDescription = description;
    }

    private Llrs(String code, String description, String longDescription) {
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