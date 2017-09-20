package Models.Inventory.Building.GEM.v2.Height.HeightTypes;

import Models.Inventory.Building.GEM.v2.Height.HeightQualifier;

public class ApproximateHeight<T extends Number> extends Height<T> {
    public T heightValue;

    public ApproximateHeight(T heightValue, HeightQualifier qualifier) {
        super(qualifier);
        this.heightValue = heightValue;
    }

    @Override
    public String getTaxonomyString() {
        return super.qualifier.code + ":" + this.heightValue.toString();
    }
}
