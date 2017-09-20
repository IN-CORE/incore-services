package Common.Range;

public class ValueRange<T extends Comparable> extends Range<T> {
    public T value;
    public ComparisonType comparisonType;

    public ValueRange(T value, ComparisonType comparisonType) {
        if (value == null) {
            throw new IllegalArgumentException("Value parameter can not be null");
        }

        this.value = value;
        this.comparisonType = comparisonType;
    }

    @Override
    public boolean contains(Comparable value) {
        return false;
    }

    @Override
    public String getMinStr() {
        if (comparisonType == ComparisonType.GreaterThan) {
            return value.toString();
        } else {
            return "∞";
        }
    }

    @Override
    public String getMaxStr() {
        if (comparisonType == ComparisonType.GreaterThan) {
            return "-∞";
        } else {
            return value.toString();
        }
    }
}
