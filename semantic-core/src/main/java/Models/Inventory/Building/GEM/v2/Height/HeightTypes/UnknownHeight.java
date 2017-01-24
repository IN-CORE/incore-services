package Models.Inventory.Building.GEM.v2.Height.HeightTypes;

import Models.Inventory.Building.GEM.v2.Common.GemAtomSerializable;
import Models.Inventory.Building.GEM.v2.Height.HeightQualifier;

public class UnknownHeight<T extends Number> extends Height<T> {
    public UnknownHeight(HeightQualifier qualifier) {
        super(qualifier);
    }

    @Override
    public String getTaxonomyString() {
        return qualifier.code;
    }

    @Override
    public GemAtomSerializable deserialize(String taxonomyString) {

    }
}
