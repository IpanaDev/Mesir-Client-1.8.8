package ipana.utils.ncp.utilities;

import ipana.utils.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;

public class VelocityData {
    private final Minecraft mc = Minecraft.getMinecraft();
    private FrictionAxisVelocity horVel = new FrictionAxisVelocity();
    private final SimpleAxisVelocity verVel = new SimpleAxisVelocity();
    public static final int ACTIVATION_TICKS = 140;
    public static final int ACTIVATION_COUNTER = 80;
    public static final int GRACE_TICKS = 20;
    public SimpleEntry verVelUsed = null;

    public void addVelocity(double vx, double vy, double vz, double adjustment) {
        int tick = Minecraft.getRunTick();
        this.removeInvalidVelocity(tick - ACTIVATION_TICKS);
        this.verVel.add(new SimpleEntry(tick, vy, ACTIVATION_COUNTER));
        if (vx != 0.0 || vz != 0.0) {
            double newVal = Math.sqrt(vx * vx + vz * vz) + adjustment;
            //PlayerUtils.debug("DEF: "+newVal+" : "+vy);
            this.horVel.add(new AccountEntry(tick, newVal, ACTIVATION_COUNTER, VelocityData.getHorVelValCount(newVal)));
        }
    }
    public static int getHorVelValCount(double velocity) {
        return Math.max(20, 1 + (int)Math.round(velocity * 10.0));
    }

    public void clearActiveHorVel() {
        this.horVel.clearActive();
    }

    public void clearHorVel() {
        this.horVel.clear();
    }

    public void removeInvalidVelocity(int tick) {
        this.horVel.removeInvalid(tick);
        this.verVel.removeInvalid(tick);
    }
    public void velocityTick(int invalidateBeforeTick) {
        this.removeInvalidVelocity(invalidateBeforeTick);
        this.horVel.tick();
        this.verVelUsed = null;
    }
    public double getHorizontalFreedom() {
        return this.horVel.getFreedom();
    }

    public double useHorizontalVelocity(double amount) {
        return this.horVel.use(amount);
    }

    public SimpleEntry useVerticalVelocity(double amount) {
        SimpleEntry available = this.verVel.use(amount, 0.0625);
        if (available != null) {
            this.verVelUsed = available;
        }
        return available;
    }


    public SimpleEntry getOrUseVerticalVelocity(double amount) {
        if (this.verVelUsed != null && this.verVel.matchesEntry(this.verVelUsed, amount, 0.0625)) {
            return this.verVelUsed;
        }
        return this.useVerticalVelocity(amount);
    }

    public FrictionAxisVelocity horizontalVelocity() {
        return horVel;
    }

    public void setHorizontalVelocity(FrictionAxisVelocity newVel) {
        this.horVel = newVel;
    }
}
