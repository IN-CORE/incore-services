package Semantic.Units;

import Semantic.Units.Conversion.Engine.ConversionEngine;
import Semantic.Units.Model.Unit;

public class Quantity implements Comparable<Quantity> {
    public Number value;
    public Unit unit;

    public Quantity(Number value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    public static Quantity of(Number value, Unit unit) {
        return new Quantity(value, unit);
    }

    public String getName() {
        return value.toString() + " " + unit.plural;
    }

    public String getUnicodeName() {
        return value.toString() + " " + unit.unicodePlural;
    }

    public Quantity convertTo(Unit convert) {
        return ConversionEngine.convert(this, convert);
    }

    // TODO
    @Override
    public int compareTo(Quantity compare) {
        /*if(this.unit.equals(compare.unit)) {
            return Number.compare(this.value, compare.value);
        } else if (this.unit.equivalentTo(compare.unit)) {

        } else if (this.unit.dimension.equivalentTo(compare.unit.dimension)) {

        } else {

        }


        // if not unknown
        // if the dimensions are equal
        // if the units are the same
        // if the units are equivalent
        // if the units are not equal/equivalent then convertTo one to the other then compare

        */
        return 0;
    }
}
