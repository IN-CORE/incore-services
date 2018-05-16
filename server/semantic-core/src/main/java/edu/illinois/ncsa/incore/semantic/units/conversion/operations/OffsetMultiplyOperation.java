
package edu.illinois.ncsa.incore.semantic.units.conversion.operations;

public class OffsetMultiplyOperation extends ReversibleOperation {
    private double factor;
    private double inverseFactor;
    private double offset;

    public OffsetMultiplyOperation(double offset, double factor) {
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
