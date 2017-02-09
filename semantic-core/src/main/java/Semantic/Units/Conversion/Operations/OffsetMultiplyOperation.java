package Semantic.Units.Conversion.Operations;

public class OffsetMultiplyOperation extends ReversibleOperation {
    private double factor;
    private double inverseFactor;
    private double offset;

    public OffsetMultiplyOperation(double factor, double offset) {
        this.factor = factor;
        this.offset = offset;
        this.inverseFactor = 1.0 / factor;
    }

    @Override
    public double invoke(Number value) {
        return ((value.doubleValue()) + offset) * factor;

    }

    @Override
    public double invokeInverse(Number value) {
        return ((value.doubleValue() * inverseFactor) - offset);
    }

    @Override
    public ReversibleOperation getInverseOperation() {
        return new MultiplyOffsetOperation(inverseFactor, -1.0 * offset);
    }
}
