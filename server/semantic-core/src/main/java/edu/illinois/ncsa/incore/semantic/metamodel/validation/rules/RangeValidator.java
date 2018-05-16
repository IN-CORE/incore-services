
package edu.illinois.ncsa.incore.semantic.metamodel.validation.rules;

public class RangeValidator<T extends Comparable> extends Validator<T> {

    @Override
    public boolean validate(T value) {
        return false;
    }
}
