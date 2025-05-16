package ipana.utils.math;

import java.lang.reflect.Array;

public class Heap<T extends Heap.IHeapItem<T>> {

    private T[] items;
    private int currentItemCount;

    public Heap(int maxHeapCount) {
        items = (T[]) Array.newInstance(Object.class,maxHeapCount);
    }

    public boolean contains(T item) {
        return items[item.getHeapIndex()].equals(item);
    }

    public void add(T item) {
        item.setHeapIndex(currentItemCount);
        items[currentItemCount] = item;
        sortUp(item);
        currentItemCount++;
    }

    public T removeFirst() {
        T firstItem = items[0];
        currentItemCount--;
        items[0] = items[currentItemCount];
        items[0].setHeapIndex(0);
        sortDown(items[0]);
        return firstItem;
    }
    public T remove(T item) {
        currentItemCount--;
        T t = items[item.getHeapIndex()];
        items[t.getHeapIndex()] = items[currentItemCount];
        items[t.getHeapIndex()].setHeapIndex(t.getHeapIndex());
        sortDown(items[0]);
        return t;
    }
    public void updateItem(T item) {
        sortUp(item);
    }

    public int getCount() {
        return currentItemCount;
    }

    private void sortDown(T item) {
        while (true) {
            int childIndexLeft = item.getHeapIndex()*2+1;
            int childIndexRight = item.getHeapIndex()*2+2;
            int swapIndex = 0;

            if (childIndexLeft < currentItemCount) {
                swapIndex = childIndexLeft;
                if (childIndexRight < currentItemCount) {
                    if (items[childIndexLeft].compareTo(items[childIndexRight]) < 0) {
                        swapIndex = childIndexRight;
                    }
                }

                if (item.compareTo(items[swapIndex]) < 0) {
                    swap(item,items[swapIndex]);
                } else {
                    return;
                }
            } else {
                return;
            }
        }
    }

    private void sortUp(T item) {
        int parentIndex = (item.getHeapIndex()-1)/2;
        while (true) {
            T parentItem = items[parentIndex];
            if (item.compareTo(parentItem) > 0) {
                swap(item,parentItem);
            } else {
                break;
            }
        }
    }

    private void swap(T itemA, T itemB) {
        items[itemA.getHeapIndex()] = itemB;
        items[itemB.getHeapIndex()] = itemA;
        int itemAIndex = itemA.getHeapIndex();
        itemA.setHeapIndex(itemB.getHeapIndex());
        itemB.setHeapIndex(itemAIndex);
    }

    public interface IHeapItem<T> extends Comparable<T> {
        int getHeapIndex();
        void setHeapIndex(int i);

    }
}
