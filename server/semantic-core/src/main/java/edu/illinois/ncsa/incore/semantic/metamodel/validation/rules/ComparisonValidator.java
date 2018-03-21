
package edu.illinois.ncsa.incore.semantic.metamodel.validation.rules;

public class ComparisonValidator<T extends Comparable> extends Validator<T> {
    protected ComparatorType comparatorType;
    protected T comparisonValue;

    public ComparisonValidator(ComparatorType comparatorType, T comparisonValue) {
        this.comparatorType = comparatorType;
        this.comparisonValue = comparisonValue;
    }

    public boolean validate(T value) {
        switch(comparatorType) {
            case Equal:
                return value.compareTo(comparisonValue) == 0;
            case NotEqual:
                return value.compareTo(comparisonValue) != 0;
            case LessThan:
                return value.compareTo(comparisonValue) < 0;
            case GreaterThan:
                return value.compareTo(comparisonValue) > 0;
            case LessThanOrEqual:
                return value.compareTo(comparisonValue) <= 0;
            case GreaterThanOrEqual:
                return value.compareTo(comparisonValue) >= 0;
            default:
                throw new IllegalArgumentException("Comparator Type" + comparatorType.getName() + " is unrecognised");
        }
    }
}
