
package edu.illinois.ncsa.incore.semantic.units.conversion.operations;

public class MultiplyOperation extends ReversibleOperation {
    private double factor;
    private double inverseFactor;

    public MultiplyOperation(double factor) {
        this.factor = factor;
        this.inverseFactor = 1.0 / factor;
    }

    @Override
    public double invoke(Number value) {
        return (value.doubleValue() * factor);
    }

    @Override
    public double invokeInverse(Number value) {
        return (value.doubleValue() * inverseFactor);
    }

    @Override
    public ReversibleOperation getInverseOperation() {
        return new MultiplyOperation(inverseFactor);
    }

    public double getFactor() {
        return factor;
    }
}
