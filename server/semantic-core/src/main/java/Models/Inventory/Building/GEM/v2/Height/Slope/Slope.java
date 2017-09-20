package Models.Inventory.Building.GEM.v2.Height.Slope;

import Models.Inventory.Building.GEM.v2.Common.GemAtomSerializable;
import Models.Inventory.Building.GEM.v2.Height.SlopeQualifier;

public abstract class Slope implements GemAtomSerializable {
    public SlopeQualifier qualifier;

    public Slope(SlopeQualifier qualifier) {
        this.qualifier = qualifier;
    }

    public abstract String getTaxonomyString();
}
