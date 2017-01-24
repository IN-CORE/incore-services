package SemanticModels.Units.Conversion.Operations;

public abstract class ReversibleOperation extends Operation {
    public abstract double invokeInverse(Number value);
    public abstract ReversibleOperation getInverseOperation();
}
