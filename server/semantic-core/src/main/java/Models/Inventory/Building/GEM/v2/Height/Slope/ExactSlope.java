package Models.Inventory.Building.GEM.v2.Height.Slope;

import Models.Inventory.Building.GEM.v2.Height.SlopeQualifier;

public class ExactSlope extends Slope {
    public int slopeValue;

    public ExactSlope(int slopeValue) {
        super(SlopeQualifier.HD);
        this.slopeValue = slopeValue;
    }

    @Override
    public String getTaxonomyString() {
        return qualifier.code + ":" + Integer.toString(slopeValue);
    }
}
