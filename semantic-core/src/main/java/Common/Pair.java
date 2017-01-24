package Common;

public class Pair<T1, T2> {
    private T1 first;
    private T2 second;

    public Pair(T1 first, T2 second) {
        this.first = first;
        this.second = second;
    }

    public static <P1, P2> Pair<P1, P2> of(P1 first, P2 second) {
        return new Pair<P1, P2>(first, second);
    }

    public T1 getFirst() {
        return this.first;
    }

    public T2 getSecond() {
        return this.second;
    }
}
