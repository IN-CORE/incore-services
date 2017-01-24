package Common.Range;

public abstract class Range<T extends Comparable> {
    /**
     * Creates a range of (value, +∞)
     */
    public static <K extends Comparable> ValueRange<K> greaterThan(K value) {
        return new ValueRange<>(value, ComparisonType.LessThan);
    }

    /**
     * Creates a range of (-∞, value)
     */
    public static <K extends Comparable> ValueRange<K> lessThan(K value) {
        return new ValueRange<>(value, ComparisonType.GreaterThan);
    }

    /**
     * Creates a range of [min, max] with an inclusive/closed bound
     */
    public static <K extends Comparable> MinMaxRange<K> between(K min, K max) {
        return new MinMaxRange<>(BoundType.Inclusive, min, max, BoundType.Inclusive);
    }

    /**
     * Creates  between two types of comparable objects
     * ex: [3,5] for Inclusive/Closed Bound or (3,5) for Exclusive/Open Bound
     */
    public static <K extends Comparable> MinMaxRange<K> between(K min, K max, BoundType boundType) {
        return new MinMaxRange<>(boundType, min, max, boundType);
    }

    /**
     * Inclusive/Closed range. ex: [3,5]
     */
    public static <K extends Comparable> MinMaxRange<K> betweenInclusive(K min, K max) {
        return new MinMaxRange<>(BoundType.Inclusive, min, max, BoundType.Inclusive);
    }

    /**
     * Exclusive/Open range. ex: (3,5)
     */
    public static <K extends Comparable> MinMaxRange<K> betweenExclusive(K min, K max) {
        return new MinMaxRange<>(BoundType.Exclusive, min, max, BoundType.Exclusive);
    }

    /**
     * Open range. ex: (3,5)
     */
    public static <K extends Comparable> MinMaxRange<K> open(K min, K max) {
        return new MinMaxRange<>(BoundType.Exclusive, min, max, BoundType.Exclusive);
    }

    /**
     * Closed range. ex: [3,5]
     */
    public static <K extends Comparable> MinMaxRange<K> closed(K min, K max) {
        return new MinMaxRange<>(BoundType.Inclusive, min, max, BoundType.Inclusive);
    }

    /**
     * Open-Closed range. ex: (3,5]
     */
    public static <K extends Comparable> MinMaxRange<K> openClosed(K min, K max) {
        return new MinMaxRange<>(BoundType.Exclusive, min, max, BoundType.Inclusive);
    }

    /**
     * Closed-Open range. ex: [3,5)
     */
    public static <K extends Comparable> MinMaxRange<K> closedOpen(K min, K max) {
        return new MinMaxRange<>(BoundType.Inclusive, min, max, BoundType.Exclusive);
    }

    // public methods
    public abstract boolean contains(T value);

    public abstract String getMinStr();

    public abstract String getMaxStr();
}
