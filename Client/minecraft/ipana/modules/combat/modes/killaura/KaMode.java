package ipana.modules.combat.modes.killaura;

import ipana.events.*;
import ipana.managements.value.Mode;
import ipana.modules.combat.KillAura;
import ipana.utils.player.RotationUtils;
import net.minecraft.entity.EntityLivingBase;

import java.util.Comparator;
import java.util.List;

public class KaMode extends Mode<KillAura> {

    public KaMode(String name, KillAura parent) {
        super(name, parent);
    }

    public void onAction(EventInputAction event) {

    }
    public void bruh(EventExcuseMeWTF event) {

    }
    public void onTravel(EventTravel event) {

    }
    public void onPre(EventPreUpdate event) {

    }
    public void onPost(EventPostUpdate event) {

    }
    public void onFrame(EventFrame event) {

    }
    public void onDisable() {

    }
    public void onEnable() {

    }

    public void onReceive(EventPacketReceive event) {

    }

    public void onSend(EventPacketSend event) {

    }
    public void onTick(EventTick event) {

    }
    public void sortTargets() {
        getParent().targets.sort(sorter());
        if (!getParent().targets.isEmpty()) {
            EntityLivingBase base = null;
            for (EntityLivingBase e : getParent().targets) {
                if (getParent().canReach(e)) {
                    base = e;
                    break;
                }
            }
            getParent().curTar = base == null ? getParent().targets.get(0) : base;
        }
    }

    public Comparator<? super EntityLivingBase> sorter() {
        return switch (getParent().sortMode.getValue()) {
            case Distance -> Comparator.comparingDouble(ent -> mc.thePlayer.getDistanceToEntity(ent));
            case Hurts -> Comparator.comparingDouble(ent -> ent.hurtResistantTime);
            case Health -> Comparator.comparingDouble(EntityLivingBase::getHealth);
            case Angle -> Comparator.comparingDouble((EntityLivingBase ent) -> {
                double posX = ent.posX - mc.thePlayer.posX;
                double posZ = ent.posZ - mc.thePlayer.posZ;
                float yaw = (float) (Math.atan2(posZ, posX) * 180.0D / 3.141592653589793D) - 90.0F;
                double xDistance = RotationUtils.getDistanceBetweenAngles(yaw, (mc.thePlayer.lastReportedYaw - 90) % 360.0F);

                return Math.sqrt(xDistance * xDistance);
            });
        };
    }
}
