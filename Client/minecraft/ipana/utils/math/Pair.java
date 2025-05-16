package ipana.utils.math;

public class Pair<F, S> {
    private F first;
    private S second;

    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Pair)) {
            return false;
        } else if (this == o) {
            return true;
        } else {
            Pair<F, S> other = (Pair<F, S>) o;
            return first.equals(other.first) && second.equals(other.second);
        }
    }

    public static <F, S> Pair<F, S> of(F first, S second) {
        return new Pair<>(first, second);
    }

    public F first() {
        return this.first;
    }

    public S second() {
        return this.second;
    }
}

