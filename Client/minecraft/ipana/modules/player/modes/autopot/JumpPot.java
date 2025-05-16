package ipana.modules.player.modes.autopot;

import ipana.events.EventMoving;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Modules;
import ipana.modules.combat.KillAura;
import ipana.modules.combat.modes.killaura.Single;
import ipana.modules.combat.modes.killaura.TickMode;
import ipana.modules.player.AutoPot;

public class JumpPot extends AutoPotMode {
    private int ticks;

    public JumpPot(AutoPot parent) {
        super("Jump", parent);
    }

    @Override
    public void onEnable() {
        ticks = -1;
    }

    @Override
    public void onMove(EventMoving event) {
        KillAura ka = Modules.KILL_AURA;
        boolean kaCheck = ka.isEnabled() && !ka.targets.isEmpty() && ka.curTar != null;
        boolean tickCheck = ka.mode.getValue() instanceof TickMode tickMode && (tickMode.ticks == 10);
        boolean singleCheck = ka.mode.getValue() instanceof Single && ka.hit;
        if (ticks < 0 && getParent().potting) {
            getParent().potting = false;
        }
        boolean isOnGround = event.getY() < 0 && !mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(event.getX(), event.getY(), event.getZ())).isEmpty();
        if (mc.thePlayer.getHealth() <= getParent().heal.getValue() && (!kaCheck || tickCheck || singleCheck) && isOnGround && !getParent().potting && getParent().isCorrectPotion(-1) != -1) {
            ticks = 4;
            getParent().potting = true;
        }
        if (ticks >= 0 && ticks <= 3) {
            event.setX(0);
            event.setZ(0);
        }
    }

    @Override
    public void onPre(EventPreUpdate event) {
        if (ticks == 4) {
            event.setPitch(90f);
            mc.thePlayer.rotationPitchHead = event.getPitch();
        } else if (ticks == 3) {
            getParent().throwPotion();
            event.setY(event.getY() + 0.4);
            mc.thePlayer.posY = event.getY();
        } else if (ticks >= 0 && ticks <= 2) {
            event.setY(event.getY() + 0.4);
            mc.thePlayer.posY = event.getY();
            event.setCancelPackets(true);
        }
        ticks--;
    }
}
