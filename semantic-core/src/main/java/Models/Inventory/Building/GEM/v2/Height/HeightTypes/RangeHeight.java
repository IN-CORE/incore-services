package Models.Inventory.Building.GEM.v2.Height.HeightTypes;

import Models.Inventory.Building.GEM.v2.Common.GemAtomSerializable;
import Models.Inventory.Building.GEM.v2.Height.HeightQualifier;
import Common.Range;

public class RangeHeight<T extends Number & Comparable> extends Height<T> {
    public Range<T> range;

    public RangeHeight(T min, T max, HeightQualifier qualifier) {
        super(qualifier);
        this.range = Range.between(min, max);
    }

    @Override
    public String getTaxonomyString() {
        return super.qualifier.code + ":" + range.getMinStr() + "," + range.getMaxStr();
    }

    @Override
    public GemAtomSerializable deserialize(String taxonomyString) {
        return null;
    }
}
