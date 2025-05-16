package ipana.utils;

public class Timer {
    private long prevMS;

    public Timer() {
        this.prevMS = 0L;
    }

    public boolean delay(final float milliSec) {
        return this.getTime() - this.prevMS >= milliSec;
    }
    public boolean delayReset(final float milliSec) {
        boolean b = this.getTime() - this.prevMS >= milliSec;
        if (b) {
            reset();
        }
        return b;
    }
    public boolean delay(final long milliSec, final TimerInterface func, final boolean... shouldReset) {
        if (this.getTime() - this.prevMS >= milliSec) {
            if (shouldReset.length > 0) {
                reset();
            }
            func.consume(this);
            return true;
        }
        return false;
    }

    public void reset() {
        this.prevMS = this.getTime();
    }

    public long getTime() {
        return System.nanoTime() / 1000000L;
    }

    public long getDifference() {
        return this.getTime() - this.prevMS;
    }

    public void setDifference(final long difference) {
        this.prevMS = this.getTime() - difference;
    }

    @FunctionalInterface
    public interface TimerInterface { void consume(Timer timer); }

}
