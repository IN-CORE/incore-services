
package edu.illinois.ncsa.incore.semantic.metamodel.validation.rules;

public abstract class Validator<T> {
    public abstract boolean validate(T value);
}
