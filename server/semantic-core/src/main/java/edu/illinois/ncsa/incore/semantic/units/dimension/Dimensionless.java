
package edu.illinois.ncsa.incore.semantic.units.dimension;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to represent dimensionless Physical Quantities, e.g. Radians
 */
public class Dimensionless extends Dimension {
    public Dimensionless(String name) {
        super(name, "1", "1");
    }

    @Override
    protected List<PowerDimension> getPowerDimensions() {
        return new ArrayList<>();
    }

    @Override
    public DerivedDimension getNormalizedDimension() {
        return new DerivedDimension(this.getPowerDimensions());
    }
}
