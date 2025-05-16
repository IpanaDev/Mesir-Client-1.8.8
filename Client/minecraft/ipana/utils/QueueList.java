package ipana.utils;

import org.lwjgl.Sys;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class QueueList<T> {
    private List<TimedItem<T>> list;
    private long timeOutValue;

    public QueueList(long timeOutValue) {
        list = new ArrayList<>();
        this.timeOutValue = timeOutValue;
    }

    public void push(T item) {
        removeTimedOut();
        list.add(new TimedItem<>(item, System.currentTimeMillis()));
    }

    public void removeFirst(Function<T, Boolean> condition) {
        removeTimedOut();
        if (list.isEmpty()) {
            return;
        }
        if (condition.apply(list.getFirst().item)) {
            list.removeFirst();
        }
    }

    public boolean removeFirstMatch(Function<T, Boolean> condition) {
        removeTimedOut();
        for (int i = 0; i < list.size(); i++) {
            if (condition.apply(list.get(i).item)) {
                list.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean hasMatch(Function<T, Boolean> condition) {
        removeTimedOut();
        for (TimedItem<T> tTimedItem : list) {
            if (condition.apply(tTimedItem.item)) {
                return true;
            }
        }
        return false;
    }

    private void removeTimedOut() {
        list.removeIf(f -> System.currentTimeMillis() - f.bornMs >= timeOutValue);
    }

    private record TimedItem<T>(T item, long bornMs) {

    }
}
