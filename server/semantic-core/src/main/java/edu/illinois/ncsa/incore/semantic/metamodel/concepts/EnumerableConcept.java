
package edu.illinois.ncsa.incore.semantic.metamodel.concepts;

import edu.illinois.ncsa.incore.semantic.metamodel.common.Enumeration;

import java.util.ArrayList;
import java.util.List;

public class EnumerableConcept<T> extends ValueConcept<T> {
    public List<Enumeration<T>> enumerations = new ArrayList<>();
}
