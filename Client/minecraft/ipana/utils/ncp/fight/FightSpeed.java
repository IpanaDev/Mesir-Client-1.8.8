package ipana.utils.ncp.fight;

import ipana.renders.settings.anticheat.Check;
import ipana.utils.player.PlayerUtils;
import net.minecraft.entity.player.EntityPlayer;

public class FightSpeed extends Check {
    private FightConfig cc = new FightConfig();
    private FightData data = new FightData(cc);

    public boolean check(EntityPlayer player, long now) {
        if (!isEnabled()) {
            return false;
        }
        boolean cancel = false;
        data.speedBuckets.add(now, 1.0f);
        long fullTime = cc.speedBucketDur * (long)cc.speedBuckets;
        float fullLag = /*cc.lag ? TickTask.getLag(fullTime, true) :*/ 1.0f;
        float total = data.speedBuckets.score(cc.speedBucketFactor) * 1000.0f / (fullLag * (float)fullTime);
        int tick = player.ticksExisted;
        if (tick < data.speedShortTermTick) {
            data.speedShortTermTick = tick;
            data.speedShortTermCount = 1;
        } else if (tick - data.speedShortTermTick < cc.speedShortTermTicks) {
            ++data.speedShortTermCount;
            /* Returns as true
            if (!cc.lag || TickTask.getLag(50L * (long)(tick - data.speedShortTermTick), true) < 1.5f) {
                ++data.speedShortTermCount;
            } else {
                data.speedShortTermTick = tick;
                data.speedShortTermCount = 1;
            }
             */
        } else {
            data.speedShortTermTick = tick;
            data.speedShortTermCount = 1;
        }
        float shortTerm = (float)data.speedShortTermCount * 1000.0f / (50.0f * (float)cc.speedShortTermTicks);
        float max = Math.max(shortTerm, total);
        if (max > (float)cc.speedLimit) {
            data.speedVL += (max - (float)cc.speedLimit);
            if (shortTerm > total) {
                PlayerUtils.debug("§cFIGHT_SPEED (SHORT) §7vl: §6"+(int)data.speedVL);
            } else if (total > shortTerm) {
                PlayerUtils.debug("§cFIGHT_SPEED (FULL) §7vl: §6"+(int)data.speedVL);
            } else {
                PlayerUtils.debug("§cFIGHT_SPEED (BOTH) §7vl: §6"+(int)data.speedVL);
            }

            cancel = true;
        } else {
            data.speedVL *= 0.96;
        }
        return cancel;
    }

    public FightData data() {
        return data;
    }

    public FightConfig cc() {
        return cc;
    }
}
