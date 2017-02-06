package Semantic.Units.Conversion.Operations;

public class OffsetOperation extends ReversibleOperation {
    private double offset;

    public OffsetOperation(double offset) {
        this.offset = offset;
    }

    @Override
    public double invoke(Number value) {
        return value.doubleValue() + offset;
    }

    @Override
    public double invokeInverse(Number value) {
        return value.doubleValue() - offset;
    }

    @Override
    public ReversibleOperation getInverseOperation() {
        return new OffsetOperation(-1.0 * offset);
    }
}
