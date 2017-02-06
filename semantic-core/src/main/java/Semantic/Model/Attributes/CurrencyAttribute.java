package Semantic.Model.Attributes;

import Semantic.Model.Validation.ValidationResult;

import java.util.List;

public class CurrencyAttribute extends Attribute<Number> {

    @Override
    public List<ValidationResult> validateValue(Number value) {
        return null;
    }
}
