package Models.Inventory.Building.GEM.v2.Height.HeightTypes;

import Models.Inventory.Building.GEM.v2.Common.GemAtomSerializable;
import Models.Inventory.Building.GEM.v2.Height.HeightQualifier;

public abstract class Height<T extends Number> implements GemAtomSerializable {
    public HeightQualifier qualifier;

    public Height(HeightQualifier qualifier) {
        this.qualifier = qualifier;
    }

    public abstract String getTaxonomyString();
}
