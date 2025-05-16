package ipana.utils.ncp.utilities;

import java.util.Arrays;

public class ActionFrequency {
    private long time;
    private long lastUpdate;
    private final boolean noAutoReset;
    private final float[] buckets;
    private final long durBucket;

    public ActionFrequency(int nBuckets, long durBucket) {
        this(nBuckets, durBucket, false);
    }

    public ActionFrequency(int nBuckets, long durBucket, boolean noAutoReset) {
        this.time = 0L;
        this.lastUpdate = 0L;
        this.buckets = new float[nBuckets];
        this.durBucket = durBucket;
        this.noAutoReset = noAutoReset;
    }

    public final void add(long now, float amount) {
        this.update(now);
        this.buckets[0] += amount;
    }

    public final void add(float amount) {
        this.buckets[0] += amount;
    }

    public final void update(long now) {
        long diff = now - this.time;
        if (now < this.lastUpdate) {
            if (!this.noAutoReset) {
                this.clear(now);
                return;
            }

            this.time = now;
        } else {
            if (diff >= this.durBucket * (long)this.buckets.length) {
                this.clear(now);
                return;
            }

            if (diff < this.durBucket) {
                return;
            }
        }

        int shift = (int)((float)diff / (float)this.durBucket);

        int i;
        for(i = 0; i < this.buckets.length - shift; ++i) {
            this.buckets[this.buckets.length - (i + 1)] = this.buckets[this.buckets.length - (i + 1 + shift)];
        }

        for(i = 0; i < shift; ++i) {
            this.buckets[i] = 0.0F;
        }

        this.time += this.durBucket * (long)shift;
        this.lastUpdate = now;
    }

    public final void clear(long now) {
        Arrays.fill(this.buckets, 0.0F);

        this.time = this.lastUpdate = now;
    }

    /** @deprecated */
    public final float getScore(float factor) {
        return this.score(factor);
    }

    /** @deprecated */
    public final float getScore(int bucket) {
        return this.bucketScore(bucket);
    }

    public final float score(float factor) {
        return this.sliceScore(0, this.buckets.length, factor);
    }

    public final float bucketScore(int bucket) {
        return this.buckets[bucket];
    }

    public final float leadingScore(int end, float factor) {
        return this.sliceScore(0, end, factor);
    }

    public final float trailingScore(int start, float factor) {
        return this.sliceScore(start, this.buckets.length, factor);
    }

    public final float sliceScore(int start, int end, float factor) {
        float score = this.buckets[start];
        float cf = factor;

        for(int i = start + 1; i < end; ++i) {
            score += this.buckets[i] * cf;
            cf *= factor;
        }

        return score;
    }

    public final void setBucket(int n, float value) {
        this.buckets[n] = value;
    }

    public final void setTime(long time) {
        this.time = time;
        this.lastUpdate = time;
    }

    public final long lastAccess() {
        return this.time;
    }

    public final long lastUpdate() {
        return this.lastUpdate;
    }

    public final int numberOfBuckets() {
        return this.buckets.length;
    }

    public final long bucketDuration() {
        return this.durBucket;
    }

    public final String toLine() {
        StringBuilder buffer = new StringBuilder(50);
        buffer.append(this.buckets.length).append(",").append(this.durBucket).append(",").append(this.time);

        for (float bucket : this.buckets) {
            buffer.append(",").append(bucket);
        }

        return buffer.toString();
    }

    public static ActionFrequency fromLine(String line) {
        String[] split = line.split(",");
        if (split.length < 3) {
            throw new RuntimeException("Bad argument length.");
        } else {
            int n = Integer.parseInt(split[0]);
            long durBucket = Long.parseLong(split[1]);
            long time = Long.parseLong(split[2]);
            float[] buckets = new float[split.length - 3];
            if (split.length - 3 != buckets.length) {
                throw new RuntimeException("Bad argument length.");
            } else {
                for(int i = 3; i < split.length; ++i) {
                    buckets[i - 3] = Float.parseFloat(split[i]);
                }

                ActionFrequency freq = new ActionFrequency(n, durBucket);
                freq.setTime(time);

                for(int i = 0; i < buckets.length; ++i) {
                    freq.setBucket(i, buckets[i]);
                }

                return freq;
            }
        }
    }
}

