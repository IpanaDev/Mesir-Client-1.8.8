package ipana.modules.movement.modes.phase;

import ipana.events.*;
import ipana.managements.module.Modules;
import ipana.modules.movement.Phase;
import net.minecraft.block.BlockAir;

import static ipana.utils.player.PlayerUtils.*;

public class NCP313 extends PhaseMode {
    public NCP313(Phase parent) {
        super("NCP 3.13", parent);
    }
    private int delay;
    private boolean sent;
    private int slowdown;

    @Override
    public void onMove(EventMoving event) {
        if (Modules.QUICK_USE.canUse()) {
            event.setX(0);
            event.setZ(0);
        }
    }

    @Override
    public void onBeforePre(EventExcuseMeWTF event) {
        mc.thePlayer.stepHeight = 0.0f;
        mc.thePlayer.noClip = true;
    }
    @Override
    public void onPre(EventPreUpdate event) {
        mc.thePlayer.stepHeight = 0.0f;
        mc.thePlayer.noClip = true;
        if (!isInBlock(0.5) && !mc.thePlayer.movementInput.sneak && isInBlock() && !(mc.theWorld.getBlockState(mc.thePlayer.getPosition()).getBlock() instanceof BlockAir)) {
            //event.setPitch(90);
        }
        slowdown--;
        if (slowdown == 0) {
            mc.thePlayer.motionX *= 2;
            mc.thePlayer.motionZ *= 2;
        }

    }

    @Override
    public void onPost(EventPostUpdate event) {
        sent = true;
        if (mc.thePlayer.movementInput.sneak) {
            mc.thePlayer.motionY = -10;
        }
        if (mc.thePlayer.isCollidedHorizontally && mc.thePlayer.movementInput.sneak && !isInBlock()) {
            packetOffset(0.062, true);
            sendOffset(0, -0.0624, 0);
            double x = mc.thePlayer.posX;
            double y = mc.thePlayer.posY - 0.0624;
            double z = mc.thePlayer.posZ;
            packetOffset(0.001, true);
            cancelFlag(x,y,z, true);
        }
        if (mc.thePlayer.movementInput.sneak && mc.thePlayer.posY == (int)mc.thePlayer.posY+0.9376 && isInBlock()) {
            sendOffset(0, -0.0001, 0);
            sendOffset(0, -0.9376, 0);
        }
        if (mc.gameSettings.keyBindSprint.pressed) {
            mc.thePlayer.motionY = 0;
        }
    }
    private void packetOffset(double speed, boolean setPos) {
        double[] c = calculate2(speed, mc.thePlayer.rotationYaw, 1);
        sendOffset(c[0], 0, c[1]);
        if (setPos) {
            mc.thePlayer.expandPos(c[0], 0, c[1]);
        }
    }
    @Override
    public void onBB(EventBoundingBox event) {
        if ((!mc.thePlayer.movementInput.sneak || isInBlock()) && event.getBoundingBox() != null && event.getBoundingBox().maxY > mc.thePlayer.boundingBox.minY && sent) {
            event.setBoundingBox(null);
        }
    }

    @Override
    public void onMove(EventMoveInput event) {

    }
    @Override
    public void onSetBack(EventSetBack event) {
        super.onSetBack(event);
    }
    @Override
    public void onEnable() {
        delay = 5;
        super.onEnable();
    }
}