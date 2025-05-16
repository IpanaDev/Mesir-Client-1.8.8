package ipana.utils.math;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class WindowedIterator<T> implements Iterable<List<T>> {
    private final T[] array;
    private final int windowSize;

    public static <X> WindowedIterator<X> iterate(X[] array, int windowSize) {
        return new WindowedIterator<>(array, windowSize);
    }

    private WindowedIterator(T[] array, int windowSize) {
        if (windowSize <= 0) {
            throw new IllegalArgumentException("Window size must be greater than 0");
        }
        this.array = array;
        this.windowSize = windowSize;
    }

    @Override
    public Iterator<List<T>> iterator() {
        return new Iterator<>() {
            private int currentIndex = 0;

            @Override
            public boolean hasNext() {
                return currentIndex < array.length;
            }

            @Override
            public List<T> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                List<T> window = new ArrayList<>(windowSize);
                for (int i = 0; i < windowSize && currentIndex < array.length; i++, currentIndex++) {
                    window.add(array[currentIndex]);
                }
                return window;
            }
        };
    }
}