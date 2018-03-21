
package edu.illinois.ncsa.incore.semantic.metamodel.attributes;

import edu.illinois.ncsa.incore.semantic.metamodel.concepts.MeasurementConcept;
import edu.illinois.ncsa.incore.semantic.units.model.Unit;

public class UnitAttribute<T extends Number> extends Attribute<T> {
    public MeasurementConcept conceptReference;
    public Unit unit;

    @Override
    public MeasurementConcept getConceptReference() {
        return conceptReference;
    }
}
