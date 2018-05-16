
package edu.illinois.ncsa.incore.semantic.metamodel.concepts;

import java.util.List;

public class ValueConcept<T> extends Concept<T> {
    public ValueConcept() {}

    // Typical Range (Value)

    // Typical Length ? (String)

    // Hard Range (Value)
    // e.g. Speed => Max Speed of Light
    // e.g. Lat => 0,180

    // Min Length (String)
    // Max Length (String)

    // Related To
    // Derived From
    // Same As
    public GeneralConcept derivedFrom;
    public List<Concept> relatedConcepts;
}
