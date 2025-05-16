package ipana.utils.ncp.utilities;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class SimpleAxisVelocity {
    private static final double marginAcceptZero = 0.005;
    private final List<SimpleEntry> queued = new LinkedList<SimpleEntry>();

    public void addToFront(SimpleEntry entry) {
        this.queued.add(0, entry);
    }

    public void add(SimpleEntry entry) {
        this.queued.add(entry);
    }

    public boolean hasQueued() {
        return !this.queued.isEmpty();
    }

    public SimpleEntry use(double amount, double tolerance) {
        Iterator<SimpleEntry> it = this.queued.iterator();
        while (it.hasNext()) {
            SimpleEntry entry = it.next();
            it.remove();
            if (!this.matchesEntry(entry, amount, tolerance)) continue;
            return entry;
        }
        return null;
    }

    public boolean matchesEntry(SimpleEntry entry, double amount, double tolerance) {
        return Math.abs(amount) <= Math.abs(entry.value) + tolerance && (amount > 0.0 && entry.value > 0.0 && amount <= entry.value + tolerance || amount < 0.0 && entry.value < 0.0 && entry.value - tolerance <= amount || amount == 0.0 && Math.abs(entry.value) <= 0.005);
    }

    public void removeInvalid(int tick) {
        Iterator<SimpleEntry> it = this.queued.iterator();
        while (it.hasNext()) {
            SimpleEntry entry = it.next();
            --entry.actCount;
            if (entry.actCount > 0 && entry.tick >= tick) continue;
            it.remove();
        }
    }

    public void clear() {
        this.queued.clear();
    }

    public void addQueued(StringBuilder builder) {
        for (SimpleEntry vel : this.queued) {
            builder.append(" ");
            builder.append(vel);
        }
    }
}
