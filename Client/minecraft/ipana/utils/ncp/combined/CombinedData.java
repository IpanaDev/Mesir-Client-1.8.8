package ipana.utils.ncp.combined;

import ipana.utils.ncp.utilities.ActionFrequency;
import ipana.utils.ncp.utilities.PenaltyTime;

public class CombinedData {
    public double improbableVL = 0.0;
    public float lastYaw;
    public long lastYawTime;
    public float sumYaw;
    public final ActionFrequency yawFreq = new ActionFrequency(3, 333L);
    public final ActionFrequency improbableCount = new ActionFrequency(20, 3000L);
    public String lastWorld = "";
    public final PenaltyTime timeFreeze = new PenaltyTime();
}
