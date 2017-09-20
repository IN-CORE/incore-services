package Semantic.Model.Attributes;

import Semantic.Model.Validation.ValidationResult;

import java.util.List;

public class CurrencyAttribute extends ValueAttribute<Number> {

    public List<ValidationResult> validateValue(Number value) {
        return null;
    }
}
