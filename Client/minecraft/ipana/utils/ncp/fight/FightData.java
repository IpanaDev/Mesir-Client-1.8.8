package ipana.utils.ncp.fight;

import ipana.utils.ncp.utilities.ActionFrequency;

public class FightData {
    public double speedVL;
    public ActionFrequency speedBuckets ;
    public int speedShortTermCount;
    public int speedShortTermTick;
    public int lastAttackedTick;
    public double lastAttackedX = Double.MAX_VALUE;
    public double lastAttackedY;
    public double lastAttackedZ;

    public FightData(FightConfig cc) {
        speedBuckets = new ActionFrequency(cc.speedBuckets, cc.speedBucketDur);
    }
}
