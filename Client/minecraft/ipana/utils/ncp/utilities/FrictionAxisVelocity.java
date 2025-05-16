package ipana.utils.ncp.utilities;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FrictionAxisVelocity {
    private static final double minValue = 0.001;
    private static final double defaultFrictionFactor = 0.93;
    private final List<AccountEntry> queued = new LinkedList<>();
    private final List<AccountEntry> active = new LinkedList<>();

    public void add(AccountEntry vel) {
        if (Math.abs(vel.value) != 0.0) {
            this.queued.add(vel);
        }
    }

    public boolean hasActive() {
        return !this.active.isEmpty();
    }

    public boolean hasQueued() {
        return !this.queued.isEmpty();
    }

    public boolean hasAny() {
        return !this.active.isEmpty() || !this.queued.isEmpty();
    }

    public void tick() {
        this.tick(0.93);
    }

    public void tick(double frictionFactor) {
        for (AccountEntry vel : this.active) {
            --vel.valCount;
            vel.sum += vel.value;
            vel.value *= frictionFactor;
        }
        for (AccountEntry accountEntry : this.queued) {
            --accountEntry.actCount;
        }
    }

    public void removeInvalid(int tick) {
        AccountEntry vel;
        Iterator<AccountEntry> it = this.active.iterator();
        while (it.hasNext()) {
            vel = it.next();
            if (vel.valCount > 0 && !(Math.abs(vel.value) <= 0.001))
                continue;
            it.remove();
        }
        it = this.queued.iterator();
        while (it.hasNext()) {
            vel = it.next();
            if (vel != null && vel.actCount > 0 && vel.tick >= tick) continue;
            it.remove();
        }
    }

    public double getFreedom() {
        double f = 0.0;
        for (AccountEntry vel : this.active) {
            f += vel.value;
        }
        return f;
    }

    public double use(double amount) {
        if (!this.active.isEmpty() && amount * this.active.getFirst().value < 0.0) {
            this.active.clear();
        }
        Iterator<AccountEntry> it = this.queued.iterator();
        double used = 0.0;
        while (it.hasNext()) {
            AccountEntry vel = it.next();
            if (vel.value * amount < 0.0) {
                it.remove();
                continue;
            }
            used += vel.value;
            this.active.add(vel);
            it.remove();
            if (!(Math.abs(used) >= Math.abs(amount))) continue;
            break;
        }
        return used;
    }

    public void clearActive() {
        this.active.clear();
    }

    public void clear() {
        this.queued.clear();
        this.active.clear();
    }

    public void addQueued(StringBuilder builder) {
        this.addVeloctiy(builder, this.queued);
    }

    public void addActive(StringBuilder builder) {
        this.addVeloctiy(builder, this.active);
    }

    private void addVeloctiy(StringBuilder builder, List<AccountEntry> entries) {
        for (AccountEntry vel : entries) {
            builder.append(" ");
            builder.append(vel);
        }
    }

    public FrictionAxisVelocity copy() {
        FrictionAxisVelocity frictionAxisVelocity = new FrictionAxisVelocity();
        for (AccountEntry queue : queued) {
            AccountEntry entry = new AccountEntry(queue.tick, queue.value, queue.actCount, queue.valCount);
            entry.sum = queue.sum;
            frictionAxisVelocity.queued.add(entry);
        }
        for (AccountEntry active : active) {
            AccountEntry entry = new AccountEntry(active.tick, active.value, active.actCount, active.valCount);
            entry.sum = active.sum;
            frictionAxisVelocity.active.add(entry);
        }
        return frictionAxisVelocity;
    }
}

