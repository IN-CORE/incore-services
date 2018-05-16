
package edu.illinois.ncsa.incore.semantic.metamodel.attributes;

import edu.illinois.ncsa.incore.semantic.metamodel.concepts.ValueConcept;

public class ValueAttribute<T> extends Attribute<T> {
    public ValueConcept conceptReference;

    public ValueConcept getConceptReference() {
        return conceptReference;
    }
}
