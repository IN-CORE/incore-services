
package edu.illinois.ncsa.incore.semantic.metamodel.attributes.enumerable;

public class Enumeration<T> {
    public T value;
    public String description;

    public Enumeration(T value, String description) {
        this.value = value;
        this.description = description;
    }
}
