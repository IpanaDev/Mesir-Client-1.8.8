package ipana.utils.math;

import java.util.ArrayList;
import java.util.List;

public class AvgList {
    private List<Double> values;
    private int capacity;
    private double sum;

    public AvgList(int capacity) {
        this.capacity = capacity;
        this.values = new ArrayList<>(capacity);
    }

    public void put(double value) {
        while (values.size() >= this.capacity) {
            double first = values.getFirst();
            values.removeFirst();
            this.sum -= first;
        }
        this.values.add(value);
        this.sum += value;
    }

    public void clear() {
        this.values.clear();
        this.sum = 0;
    }

    public double checkUp(double value) {
        double preSum = this.sum;
        if (this.values.size() == capacity) {
            preSum -= this.values.getFirst();
        }
        return (preSum + value) / Math.min(this.values.size() + 1, capacity);
    }

    public double average() {
        if (this.values.isEmpty()) {
            return 0;
        }
        return this.sum / this.values.size();
    }
}
