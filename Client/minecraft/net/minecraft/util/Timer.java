package net.minecraft.util;

public class Timer {
    /**
     * The number of timer ticks per second of real time
     */
    float ticksPerSecond;

    /**
     * The time reported by the high-resolution clock at the last call of updateTimer(), in seconds
     */
    private double lastHRTime;

    /**
     * How many full ticks have turned over since the last call to updateTimer(), capped at 10.
     */
    public int elapsedTicks;

    /**
     * How much time has elapsed since the last tick, in ticks, for use by display rendering routines (range: 0.0 -
     * 1.0).  This field is frozen if the display is paused to eliminate jitter.
     */
    public float renderPartialTicks;

    /**
     * A multiplier to make the timer (and therefore the game) go faster or slower.  0.5 makes the game run at half-
     * speed.
     */
    public float timerSpeed = 1.0F;

    /**
     * How much time has elapsed since the last tick, in ticks (range: 0.0 - 1.0).
     */
    public float elapsedPartialTicks;

    /**
     * The time reported by the system clock at the last sync, in milliseconds
     */
    private double lastSyncSysClock;

    /**
     * The time reported by the high-resolution clock at the last sync, in milliseconds
     */
    private double lastSyncHRClock;
    private double field_74285_i;

    /**
     * A ratio used to sync the high-resolution clock to the system clock, updated once per second
     */
    private double timeSyncAdjustment = 1.0D;

    private long lastTickNano = -1;
    private float ticksPerMS;
    private float adjustment;
    private float lastDiff;

    public Timer(float p_i1018_1_) {
        this.ticksPerSecond = p_i1018_1_;
        this.ticksPerMS = 1000f / this.ticksPerSecond;
        this.lastSyncHRClock = System.nanoTime() / 1000000.0;
        this.lastSyncSysClock = lastSyncHRClock;
    }

    /**
     * Updates all fields of the Timer using the current time
     */

    public void updateTimer(double adjustment, long nano) {
        /*if (this.lastTickNano == -1) {
            this.lastTickNano = nano;
        }
        if (this.adjustment >= 1000) {
            this.adjustment = 0;
        }
        float diffInMs = (nano-this.lastTickNano) / 1000000f;
        float timeDiff = diffInMs + this.adjustment;
        float tickMS = this.ticksPerMS*this.timerSpeed;
        float partialTicks = timeDiff / tickMS;
        this.elapsedTicks = (int) partialTicks;
        this.renderPartialTicks = MathHelper.clamp_float(partialTicks, 0, 1);
        if (timeDiff >= tickMS) {
            float diff = diffInMs-tickMS;
            if (diff >= 0) {
                this.adjustment += diff;
            } else {
                if (this.adjustment >= -diff) {
                    this.adjustment -= -diff;
                } else {
                    this.adjustment = 0;
                }
            }
            System.out.println(timeDiff+", "+tickMS+", "+diffInMs+", "+this.adjustment+", "+this.renderPartialTicks);
            this.lastTickNano = nano;
        }*/

        double currentMsNano = nano / 1000000.0;
        double j = currentMsNano - this.lastSyncSysClock;
        double currentSecond = currentMsNano / 1000.0D;

        if (j <= 1000L && j >= 0) {
            this.field_74285_i += j;

            if (this.field_74285_i > 1000) {
                double l = currentMsNano - this.lastSyncHRClock;
                double d1 = this.field_74285_i / l;
                this.timeSyncAdjustment += (d1 - this.timeSyncAdjustment) * adjustment;
                this.lastSyncHRClock = currentMsNano;
                this.field_74285_i = 0;
            }

            if (this.field_74285_i < 0) {
                this.lastSyncHRClock = currentMsNano;
            }
        } else {
            this.lastHRTime = currentSecond;
        }

        this.lastSyncSysClock = currentMsNano;
        double d2 = (currentSecond - this.lastHRTime) * this.timeSyncAdjustment;
        this.lastHRTime = currentSecond;
        d2 = MathHelper.clamp_double(d2, 0.0D, 1.0D);
        this.elapsedPartialTicks = (float) (this.elapsedPartialTicks + d2 * this.timerSpeed * this.ticksPerSecond);
        this.elapsedTicks = (int) this.elapsedPartialTicks;
        this.elapsedPartialTicks -= (float) this.elapsedTicks;

        if (this.elapsedTicks > 10) {
            this.elapsedTicks = 10;
        }

        this.renderPartialTicks = this.elapsedPartialTicks;
    }
}
