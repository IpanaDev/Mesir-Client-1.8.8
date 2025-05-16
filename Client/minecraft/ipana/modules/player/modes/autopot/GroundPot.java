package ipana.modules.player.modes.autopot;

import ipana.events.EventMoving;
import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Modules;
import ipana.modules.combat.KillAura;
import ipana.modules.combat.modes.killaura.Single;
import ipana.modules.combat.modes.killaura.TickMode;
import ipana.modules.player.AutoPot;
import net.minecraft.client.Minecraft;

public class GroundPot extends AutoPotMode {
    private int waitTicks;

    public GroundPot(AutoPot parent) {
        super("Ground", parent);
    }

    @Override
    public void onMove(EventMoving event) {
        KillAura ka = Modules.KILL_AURA;
        getParent().potting = false;
        boolean kaCheck = ka.isEnabled() && !ka.targets.isEmpty() && ka.curTar != null;
        boolean tickCheck = ka.mode.getValue() instanceof TickMode tickMode && tickMode.ticks > 4;
        boolean singleCheck = ka.mode.getValue() instanceof Single && ka.hit;

        if (Minecraft.getRunTick()-waitTicks >= 7 && mc.thePlayer.getHealth() <= getParent().heal.getValue() && (!kaCheck || tickCheck || singleCheck) && getParent().isCorrectPotion(-1) != -1) {
            getParent().potting = true;
            waitTicks = Minecraft.getRunTick();
        }
    }

    @Override
    public void onPre(EventPreUpdate event) {
        if (getParent().potting) {
            double xDist = (mc.thePlayer.posX - mc.thePlayer.prevPosX) * 6;
            double zDist = (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * 6;
            double yDist = 1;
            double speed = Math.hypot(xDist, zDist);
            float yaw = (float) ((Math.atan2(zDist, xDist) * 180.0D / (Math.PI)) - 90.0F);
            float pitch = (float) (-(Math.atan2(yDist, speed) * 180.0D / (Math.PI)));
            event.setYaw(yaw);
            event.setPitch(90f);//TODO: speed prediction
            mc.thePlayer.rotationYawHead = event.getYaw();
            mc.thePlayer.renderYawOffset = event.getYaw();
            mc.thePlayer.rotationPitchHead = event.getPitch();
        }
    }

    @Override
    public void onPost(EventPostUpdate event) {
        if (getParent().potting) {
            getParent().throwPotion();
            KillAura ka = Modules.KILL_AURA;
            boolean kaCheck = ka.isEnabled() && !ka.targets.isEmpty() && ka.curTar != null;
            if (kaCheck && ka.improbable.getValue() && ka.mode.getValue() instanceof TickMode tickMode) {
                tickMode.ticks++;
            }
        }
    }
}
