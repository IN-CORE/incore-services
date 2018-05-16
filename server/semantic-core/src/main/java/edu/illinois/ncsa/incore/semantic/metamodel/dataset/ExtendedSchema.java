
package edu.illinois.ncsa.incore.semantic.metamodel.dataset;

public class ExtendedSchema extends Schema {
    private Schema parent;

    public ExtendedSchema(Schema parent) {
        this.parent = parent;
    }

    // get Concept will return it's concepts and the inherited concepts
}
