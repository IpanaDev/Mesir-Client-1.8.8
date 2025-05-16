package ipana.utils.ncp.utilities;

public class PenaltyTime {
    private long penaltyLast = 0L;
    private long penaltyEnd = 0L;

    public PenaltyTime() {
    }

    public void applyPenalty(long duration) {
        this.applyPenalty(System.currentTimeMillis(), duration);
    }

    public void applyPenalty(long now, long duration) {
        this.penaltyLast = now;
        if (now < this.penaltyLast) {
            this.penaltyEnd = now + duration;
        } else {
            this.penaltyEnd = Math.max(now + duration, this.penaltyEnd);
        }
    }

    public boolean isPenalty() {
        return this.isPenalty(System.currentTimeMillis());
    }

    public boolean isPenalty(long now) {
        if (now < this.penaltyLast) {
            this.resetPenalty();
            return false;
        } else {
            return now < this.penaltyEnd;
        }
    }

    public void resetPenalty() {
        this.penaltyLast = 0L;
        this.penaltyEnd = 0L;
    }
}

