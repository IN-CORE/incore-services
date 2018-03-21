package edu.illinois.ncsa.incore.semantic.metamodel.attributes;

import edu.illinois.ncsa.incore.semantic.metamodel.validation.result.ValidationResult;

import java.util.Currency;
import java.util.List;

public class CurrencyAttribute extends ValueAttribute<Number> {
    public Currency currency;


    public List<ValidationResult> validateValue(Number value) {

        return null;
    }
}
