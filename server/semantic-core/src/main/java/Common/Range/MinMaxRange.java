package Common.Range;

public class MinMaxRange<T extends Comparable> extends Range<T> {
    public BoundType lowerBoundType;
    public T min;

    public BoundType upperBoundType;
    public T max;

    public MinMaxRange(BoundType lowerBoundType, T min, T max, BoundType upperBoundType) {
        if (min == null) {
            throw new IllegalArgumentException("Minimum value parameter can not be null");
        }

        if (max == null) {
            throw new IllegalArgumentException("Maximum value parameter can not be null");
        }

        this.lowerBoundType = lowerBoundType;
        this.upperBoundType = upperBoundType;

        this.min = min;
        this.max = max;
    }

    @Override
    public boolean contains(T value) {
        return false;
    }

    @Override
    public String getMinStr() {
        return min.toString();
    }

    @Override
    public String getMaxStr() {
        return max.toString();
    }
}
