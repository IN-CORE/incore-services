package edu.illinois.ncsa.incore.semantic.metamodel.attributes;

import edu.illinois.ncsa.incore.semantic.metamodel.common.Enumeration;
import edu.illinois.ncsa.incore.semantic.metamodel.concepts.EnumerableConcept;

import java.util.ArrayList;
import java.util.List;

public class EnumerableAttribute<T> extends Attribute<T> {
    public EnumerableConcept conceptReference;
    public List<Enumeration<T>> enumerations = new ArrayList<>();

    public EnumerableAttribute() {}

    public EnumerableAttribute(String name, String description, List<Enumeration<T>> enumerations) {
        super.name = name;
        super.description = description;
        this.enumerations = enumerations;

        super.isUnique = false;
    }

    @Override
    public EnumerableConcept getConceptReference() {
        return conceptReference;
    }
}
