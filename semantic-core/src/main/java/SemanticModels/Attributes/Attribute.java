package SemanticModels.Attributes;

import SemanticModels.Attributes.Common.Definition;
import SemanticModels.Validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;

public abstract class Attribute<T> {
    public String name;
    public String uri;
    public List<Definition> Definitions;
    public boolean IsNullable;
    public boolean HasUnits;
    public boolean IsRequired;
    public boolean IsUnique;
    public List<String> aliases;

    // is computed field
    // relationships, inferrences

    public abstract List<ValidationResult> validateValue(T value);

    public List<ValidationResult> validateValues(List<? extends T> values) {
        List<ValidationResult> validationResults = new ArrayList<>();

        for (T value : values) {
            validationResults.addAll(validateValue(value));
        }

        return validationResults;
    }
}

