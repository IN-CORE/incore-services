
package edu.illinois.ncsa.incore.semantic.metamodel.attributes.enumerable;

import edu.illinois.ncsa.incore.semantic.metamodel.attributes.Attribute;
import edu.illinois.ncsa.incore.semantic.metamodel.concepts.Concept;

import java.util.ArrayList;
import java.util.List;

public class EnumerableAttribute<T> extends Attribute {

    public List<Enumeration<T>> enumerations = new ArrayList<>();

    public EnumerableAttribute() {

    }

    public EnumerableAttribute(String name, String description, List<Enumeration<T>> enumerations) {
        super.name = name;
        super.description = description;
        this.enumerations = enumerations;

        super.isUnique = false; // TODO may not always be the case
    }

    @Override
    public Concept getConceptReference() {
        return null;
    }
}
