package Semantic.Model.Attributes;

public class ValueAttribute<T> extends Attribute {
    public T value;


    // public abstract List<ValidationResult> validateValue(T value);

//    public List<ValidationResult> validateValues(List<? extends T> values) {
//        List<ValidationResult> validationResults = new ArrayList<>();
//
//        for (T value : values) {
//            validationResults.addAll(validateValue(value));
//        }
//
//        return validationResults;
//    }
}
