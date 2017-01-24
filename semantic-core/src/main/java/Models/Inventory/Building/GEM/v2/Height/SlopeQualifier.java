package Models.Inventory.Building.GEM.v2.Height;

import Models.Inventory.Building.GEM.v2.Common.Qualifier;

public class SlopeQualifier extends Qualifier {
    public static SlopeQualifier HD99 = new SlopeQualifier("HBAPP", "Unknown", "Slope of the ground unknown");
    public static SlopeQualifier HD = new SlopeQualifier("HD", "Slope", "Slope of the ground");

    private SlopeQualifier(String code, String description, String shortDescription) {
        super(code, description, shortDescription);
    }

    private SlopeQualifier(String code, String description) {
        super(code, description);
    }
}
