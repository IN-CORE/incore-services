package SemanticModels.Attributes;

//import SemanticModels.Units.Unit;
import SemanticModels.Validation.ValidationResult;

import java.util.List;

public class UnitAttribute extends Attribute<Number> {
    //public Unit unit;

    @Override
    public List<ValidationResult> validateValue(Number value) {
        return null;
    }
}
