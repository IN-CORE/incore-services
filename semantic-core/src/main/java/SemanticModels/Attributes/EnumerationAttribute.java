package SemanticModels.Attributes;

import SemanticModels.Attributes.Common.Enumeration;
import SemanticModels.Validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;

public class EnumerationAttribute extends Attribute<String> {
    List<Enumeration> Enumerables = new ArrayList<>();

    @Override
    public List<ValidationResult> validateValue(String value) {
        return null;
    }
}
