package ipana.utils.ncp.combined;

import ipana.renders.settings.anticheat.Check;
import ipana.utils.player.PlayerUtils;

public class Improbable extends Check {
    private CombinedConfig cc;
    private CombinedData data;

    public Improbable(CombinedConfig cc, CombinedData data) {
        this.cc = cc;
        this.data = data;
    }

    public void feed( float weight, long now) {
        data.improbableCount.add(now, weight);
    }

    public boolean checkImprobable(float weight, long now) {
        if (!isEnabled()) {
            return false;
        }
        double full;
        data.improbableCount.add(now, weight);
        float shortTerm = data.improbableCount.bucketScore(0);
        double violation = 0.0;
        boolean violated = false;
        if ((double)(shortTerm * 0.8f) > (double)cc.improbableLevel / 20.0) {
            //float lag = cc.lag ? TickTask.getLag(data.improbableCount.bucketDuration(), true) : 1.0F;
            float lag = 1.0F;
            if ((double)(shortTerm / lag) > (double)cc.improbableLevel / 20.0) {
                violation += (double)shortTerm * 2.0 / (double)lag;
                violated = true;
            }
        }
        if ((full = data.improbableCount.score(1.0f)) > (double)cc.improbableLevel) {
            //float lag = cc.lag ? TickTask.getLag(data.improbableCount.bucketDuration() * (long)data.improbableCount.numberOfBuckets(), true) : 1.0F;
            float lag = 1.0F;
            if (full / (double)lag > (double)cc.improbableLevel) {
                violation += full / (double)lag;
                violated = true;
            }
        }
        boolean cancel = false;
        if (violated) {
            data.improbableVL += violation / 10.0;
            PlayerUtils.debug("§cIMPROBABLE_SPEED §7vl: §6"+(int)data.improbableVL);
            data.improbableVL = 0;
            cancel = true;
        } else {
            data.improbableVL *= 0.95;
        }
        return cancel;
    }
}
