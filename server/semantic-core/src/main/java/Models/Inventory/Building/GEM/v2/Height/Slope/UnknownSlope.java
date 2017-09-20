package Models.Inventory.Building.GEM.v2.Height.Slope;

import Models.Inventory.Building.GEM.v2.Height.SlopeQualifier;

public class UnknownSlope extends Slope {
    public UnknownSlope() {
        super(SlopeQualifier.HD99);
    }

    @Override
    public String getTaxonomyString() {
        return qualifier.code;
    }
}
