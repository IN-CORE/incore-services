package Semantic.Units.Conversion.Operations;

public class ScaleOperation extends ReversibleOperation {
    private int scaleFactor;
    private int inverseScaleFactor;

    public ScaleOperation(int scaleFactor) {
        this.scaleFactor = scaleFactor;
        this.inverseScaleFactor = -1 * scaleFactor;
    }

    @Override
    public double invoke(Number value) {
        return (value.doubleValue() * Math.pow(10, scaleFactor));
    }

    @Override
    public double invokeInverse(Number value) {
        return (value.doubleValue() * Math.pow(10, inverseScaleFactor));
    }

    @Override
    public ReversibleOperation getInverseOperation() {
        return new ScaleOperation(inverseScaleFactor);
    }
}
