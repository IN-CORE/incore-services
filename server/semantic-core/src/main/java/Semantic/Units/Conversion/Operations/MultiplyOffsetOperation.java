package Semantic.Units.Conversion.Operations;

public class MultiplyOffsetOperation extends ReversibleOperation {
    private double factor;
    private double inverseFactor;
    private double offset;

    public MultiplyOffsetOperation(double factor, double offset) {
        this.factor = factor;
        this.offset = offset;
        this.inverseFactor = 1.0 / factor;
    }

    @Override
    public double invoke(Number value) {
        return ((value.doubleValue() * factor) + offset);
    }

    @Override
    public double invokeInverse(Number value) {
        return ((value.doubleValue()) - offset) * inverseFactor;
    }

    @Override
    public ReversibleOperation getInverseOperation() {
        return new OffsetMultiplyOperation(inverseFactor, -1.0 * offset);
    }
}
