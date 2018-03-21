
package edu.illinois.ncsa.incore.semantic.metamodel.concepts;

import edu.illinois.ncsa.incore.semantic.units.dimension.Dimension;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

import java.util.ArrayList;
import java.util.List;

public class MeasurementConcept extends ValueConcept<Number> {
    public Dimension dimension;
    public List<Unit> commonUnits = new ArrayList<>();

    public Dimension getDimension() {
        return dimension;
    }

    public List<Unit> commonUnits() {
        return commonUnits;
    }
}
