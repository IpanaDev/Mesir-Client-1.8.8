package ipana.modules.combat.modes.killaura;

import ipana.events.EventInputAction;
import ipana.events.EventPreUpdate;
import ipana.events.EventTick;
import ipana.events.EventTravel;
import ipana.modules.combat.KillAura;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MovingObjectPosition;

public class Legit extends KaMode{
    private Minecraft mc = Minecraft.getMinecraft();
    private int blockTicks;
    private float changedYaw;
    private float changedPitch;
    private boolean rotationChanged;

    public Legit(KillAura parent) {
        super("Legit",parent);
    }

    private boolean reachBoost() {
        return false;//bravo grim
    }

    @Override
    public void onTick(EventTick event) {
        KillAura ka = getParent();
        blockTicks--;
        if (blockTicks == 0) {
            mc.gameSettings.keyBindUseItem.pressed = false;
        }
        if (mc.thePlayer == null || ka.targets.isEmpty() || ka.curTar == null) {
            return;
        }
        if (ka.curTar.hurtResistantTime == 11) {
            mc.thePlayer.stopSprint = 1;
        }
        if (mc.thePlayer.hurtResistantTime >= 10 && mc.thePlayer.hurtResistantTime <= 12 && ka.autoBlock.getValue() && ka.canBlock()) {
            blockTicks = 2;
            mc.gameSettings.keyBindUseItem.pressed = true;
        }
        super.onTick(event);
    }

    @Override
    public void onAction(EventInputAction event) {
        KillAura ka = getParent();
        if (mc.thePlayer == null || ka.targets.isEmpty() || ka.curTar == null) {
            return;
        }
        boolean boostedReach = false;
        var mouseOver = PlayerUtils.getMouseOver(changedYaw, changedPitch, false);
        if ((mouseOver == null || mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) && reachBoost()) {
            boostedReach = true;
            mouseOver = PlayerUtils.getMouseOver(changedYaw, changedPitch, true);
        }
        if (mouseOver != null && mouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
            if (boostedReach) {
                PlayerUtils.debug("Reach increased.");
            }
            if (Minecraft.getRunTick() % 3 != 0 && !mc.thePlayer.isUsingItem()) {
                mc.thePlayer.swingItem();
                mc.playerController.attackEntity(mc.thePlayer, mouseOver.entityHit);
            }
        }
    }

    @Override
    public void onTravel(EventTravel event) {
        KillAura ka = getParent();
        if (!ka.targets.isEmpty()) {
            ka.curTar = ka.targets.get(0);
            if (mc.thePlayer.getDistanceToEntity(ka.curTar) <= ka.range.getValue()) {
                //float yawDiff = Math.clamp(RotationUtils.getYawChange(ka.curTar, mc.thePlayer.rotationYaw), -90, 90);
                changedYaw = mc.thePlayer.rotationYaw + RotationUtils.getYawChange(ka.curTar, mc.thePlayer.rotationYaw);
                changedPitch = mc.thePlayer.rotationPitch + RotationUtils.getPitchChange(ka.curTar, mc.thePlayer.rotationPitch);
                rotationChanged = true;
                event.setYaw(changedYaw);
            }
        }
    }

    @Override
    public void onPre(EventPreUpdate event) {
        if (rotationChanged) {
            event.setYaw(changedYaw);
            event.setPitch(changedPitch);
            rotationChanged = false;
        }
    }

    public void onDisable() {
        KillAura ka = getParent();
        ka.hit = false;
    }
    public void onEnable() {
        KillAura ka = getParent();
        ka.hit = false;
    }
}
