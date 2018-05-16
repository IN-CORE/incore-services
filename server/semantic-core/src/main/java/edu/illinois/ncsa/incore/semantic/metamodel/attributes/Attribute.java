
package edu.illinois.ncsa.incore.semantic.metamodel.attributes;

import edu.illinois.ncsa.incore.semantic.metamodel.concepts.Concept;

public abstract class Attribute<T> {
    public String fieldName;
    public String name;
    public String description;

    public boolean isNullable = true;
    public boolean isUnique = false;

    public abstract <I extends Concept> I getConceptReference();
}
