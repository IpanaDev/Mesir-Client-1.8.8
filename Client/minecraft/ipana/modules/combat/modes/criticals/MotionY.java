package ipana.modules.combat.modes.criticals;

import ipana.events.EventPreUpdate;
import ipana.managements.module.Modules;
import ipana.modules.combat.Criticals;
import ipana.modules.combat.KillAura;
import ipana.utils.player.PlayerUtils;

public class MotionY extends CritMode {

    public MotionY(Criticals parent) {
        super("MotionY",parent);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        Criticals criticals = getParent();
        KillAura ka = Modules.KILL_AURA;
        double critY = criticals.critY.getValue();
        boolean gayCheck = (Modules.KILL_AURA.isEnabled() ? (ka.targets.size() > 0 && mc.thePlayer.getDistanceToEntity(ka.curTar) <= ka.range.getValue()) : mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null && mc.thePlayer.isSwingInProgress);
        boolean gayCheckV3 = Modules.SPEED.isEnabled() && PlayerUtils.isMoving2();
        if (gayCheck) {
            if (!gayCheckV3 && mc.thePlayer.isCollidedVertically) {
                mc.thePlayer.motionY = critY;
            }
            if (!mc.thePlayer.isCollidedVertically) {
                event.setOnGround(false);
            }
        }
        super.onPre(event);
    }
}
