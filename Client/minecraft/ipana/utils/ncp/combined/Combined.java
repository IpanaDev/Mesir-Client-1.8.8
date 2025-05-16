package ipana.utils.ncp.combined;

import ipana.utils.player.PlayerUtils;

public class Combined {
    private CombinedConfig cc = new CombinedConfig();
    private CombinedData data = new CombinedData();
    private Improbable improbable = new Improbable(cc,data);

    public void feedYawRate(float yaw, long now, boolean worldChanged) {
        float yawDiff;
        if (yaw <= -360.0f) {
            yaw = -(-yaw % 360.0f);
        } else if (yaw >= 360.0f) {
            yaw %= 360.0f;
        }
        if (worldChanged) {
            data.lastYaw = yaw;
            data.sumYaw = 0.0f;
            data.lastYawTime = now;
        }
        if ((yawDiff = data.lastYaw - yaw) < -180.0f) {
            yawDiff += 360.0f;
        } else if (yawDiff > 180.0f) {
            yawDiff -= 360.0f;
        }
        long elapsed = now - data.lastYawTime;
        data.lastYaw = yaw;
        data.lastYawTime = now;
        float dAbs = Math.abs(yawDiff);
        float stationary = 32.0f;
        if (dAbs < stationary) {
            data.sumYaw += yawDiff;
            if (Math.abs(data.sumYaw) < stationary) {
                data.yawFreq.update(now);
                return;
            }
            data.sumYaw = 0.0f;
        } else {
            data.sumYaw = 0.0f;
        }
        float dNorm = dAbs / (float)(1L + elapsed);
        data.yawFreq.add(now, dNorm);
    }

    public boolean checkYawRate(float yaw, long now, boolean worldChanged) {
        if (!improbable.isEnabled()) {
            return false;
        }
        feedYawRate(yaw, now, worldChanged);
        float threshold = cc.yawRate;
        float stScore = data.yawFreq.bucketScore(0) * 3.0f;
        //float stViol = stScore > threshold ? (!cc.lag || (double)TickTask.getLag(data.yawFreq.bucketDuration(), true) < 1.2 ? stScore : 0.0f) : 0.0f;
        float stViol = stScore > threshold ? stScore : 0.0f;
        float fullScore = data.yawFreq.score(1.0f);
        //float fullViol = fullScore > threshold ? (cc.lag ? fullScore / TickTask.getLag(data.yawFreq.bucketDuration() * (long)data.yawFreq.numberOfBuckets(), true) : fullScore) : 0.0f;
        float fullViol = fullScore > threshold ? fullScore : 0.0f;
        float total = Math.max(stViol, fullViol);
        boolean cancel = false;
        if (total > threshold) {
            float amount = (total - threshold) / threshold * 1000.0f;
            data.timeFreeze.applyPenalty(now, (long)Math.min(Math.max(cc.yawRatePenaltyFactor * amount, (float)cc.yawRatePenaltyMin), (float)cc.yawRatePenaltyMax));
            if (improbable.checkImprobable(amount / 100.0f, now)) {
                cancel = true;
            }
        }
        if (data.timeFreeze.isPenalty()) {
            cancel = true;
        }
        if (cancel) {
            PlayerUtils.debug("§cCOMBINED_YAW §7vl: §6"+(int)total);
        }
        return cancel;
    }

    public void resetYawRate(float yaw, long time, boolean clear) {
        if (yaw <= -360.0f) {
            yaw = -(-yaw % 360.0f);
        } else if (yaw >= 360.0f) {
            yaw %= 360.0f;
        }
        data.lastYaw = yaw;
        data.lastYawTime = time;
        data.sumYaw = 0.0f;
        if (clear) {
            data.yawFreq.clear(time);
        }
    }

    public boolean checkYawRate(float yaw, long now, boolean worldChanged, boolean yawRateCheck) {
        if (yawRateCheck) {
            return checkYawRate(yaw, now, worldChanged);
        }
        feedYawRate(yaw, now, worldChanged);
        return false;
    }

    public Improbable improbable() {
        return improbable;
    }

    public CombinedConfig cc() {
        return cc;
    }
}
