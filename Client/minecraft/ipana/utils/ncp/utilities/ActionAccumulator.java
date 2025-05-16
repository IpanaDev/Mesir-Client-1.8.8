package ipana.utils.ncp.utilities;

public class ActionAccumulator {
    private final int[] counts;
    private final float[] buckets;
    private final int bucketCapacity;

    public ActionAccumulator(int nBuckets, int bucketCapacity) {
        this.counts = new int[nBuckets];
        this.buckets = new float[nBuckets];
        this.bucketCapacity = bucketCapacity;
    }

    public void add(float value) {
        if (this.counts[0] >= this.bucketCapacity) {
            this.shift();
        }

        ++this.counts[0];
        this.buckets[0] += value;
    }

    private void shift() {
        for (int i = this.buckets.length - 1; i > 0; --i) {
            this.counts[i] = this.counts[i - 1];
            this.buckets[i] = this.buckets[i - 1];
        }

        this.counts[0] = 0;
        this.buckets[0] = 0.0F;
    }

    public float score() {
        float score = 0.0F;

        for (int i = 0; i < this.buckets.length; ++i) {
            score += this.buckets[i];
        }

        return score;
    }

    public int count() {
        int count = 0;

        for (int i = 0; i < this.counts.length; ++i) {
            count += this.counts[i];
        }

        return count;
    }

    public void clear() {
        for (int i = 0; i < this.buckets.length; ++i) {
            this.counts[i] = 0;
            this.buckets[i] = 0.0F;
        }

    }

    public int bucketCount(int bucket) {
        return this.counts[bucket];
    }

    public float bucketScore(int bucket) {
        return this.buckets[bucket];
    }

    public int numberOfBuckets() {
        return this.buckets.length;
    }

    public int bucketCapacity() {
        return this.bucketCapacity;
    }

    public void changeBucket(int bucket, float value) {
        this.buckets[bucket] = value;
    }

    public ActionAccumulator copy() {
        ActionAccumulator actionAccumulator = new ActionAccumulator(counts.length, bucketCapacity);
        System.arraycopy(counts, 0, actionAccumulator.counts, 0, counts.length);
        System.arraycopy(buckets, 0, actionAccumulator.buckets, 0, buckets.length);
        return actionAccumulator;
    }
}
