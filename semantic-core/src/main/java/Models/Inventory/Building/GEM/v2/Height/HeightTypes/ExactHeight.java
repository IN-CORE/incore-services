package Models.Inventory.Building.GEM.v2.Height.HeightTypes;

import Models.Inventory.Building.GEM.v2.Height.HeightQualifier;

public class ExactHeight<T extends Number> extends Height<T> {
    public T heightValue;

    public ExactHeight(T value, HeightQualifier qualifier) {
        super(qualifier);
        this.heightValue = value;
    }

    public String getTaxonomyString() {
        return super.qualifier.code + ":" + this.heightValue.toString();
    }
}
