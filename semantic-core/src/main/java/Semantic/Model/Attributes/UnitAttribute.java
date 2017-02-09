package Semantic.Model.Attributes;

import Semantic.Units.Model.Unit;
import Semantic.Model.Validation.ValidationResult;

import java.util.List;

public class UnitAttribute extends ValueAttribute<Number> {
    public Unit unit;

    public List<ValidationResult> validateValue(Number value) {
        return null;
    }
}
