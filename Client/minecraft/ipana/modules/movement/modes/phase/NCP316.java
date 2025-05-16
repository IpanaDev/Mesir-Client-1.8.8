package ipana.modules.movement.modes.phase;


import ipana.events.*;
import ipana.modules.movement.Phase;
import ipana.utils.math.MathUtils;
import ipana.utils.player.PlayerUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;

import java.util.ArrayList;

import static ipana.utils.player.PlayerUtils.*;

public class NCP316 extends PhaseMode {
    public NCP316(Phase parent) {
        super("NCP 3.16", parent);
    }
    private int jumpTicks;
    private boolean canPhase;

    @Override
    public void onEnable() {
        super.onEnable();
    }

    @Override
    public void onMove(EventMoving event) {
        Phase phase = getParent();
        if (phase.passable.getValue()) {

        } else {
            if (mc.gameSettings.keyBindJump.pressed && isInBlock()) {
                double y = mc.thePlayer.motionY;
                if (mc.thePlayer.onGround || jumpTicks == 2) {
                    y = 0.42;
                    jumpTicks = 0;
                } else {
                    jumpTicks++;
                    if (jumpTicks == 1) {
                        y = 0.33;
                    } else if (jumpTicks == 2) {
                        y = 0.25;
                    }
                }
                event.setY(mc.thePlayer.motionY = y);
            }
            if (!mc.gameSettings.keyBindSneak.pressed && !mc.gameSettings.keyBindJump.pressed && isInBlock()) {
                double[] speeds = calculate(getBaseMoveSpeed());
                event.setX(speeds[0]);
                event.setZ(speeds[1]);
            }
        }
    }
    @Override
    public void onPre(EventPreUpdate event) {
        Phase phase = getParent();
        mc.thePlayer.noClip = true;
        if (!isInBlock() && mc.thePlayer.isCollidedHorizontally && PlayerUtils.isMoving2()) {
            float off = 45;
            event.setPitch(event.getPitch() + off);
            if (event.getPitch() > 90) {
                event.setPitch(-off);
            }
            canPhase = true;
        }
    }

    @Override
    public void onPost(EventPostUpdate event) {
        Phase phase = getParent();
        mc.timer.timerSpeed = 1f;
        if (canPhase) {
            if (phase.passable.getValue()) {
                PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                packetOffset(0.005);
                double x = mc.thePlayer.posX;
                double y = mc.thePlayer.posY;
                double z = mc.thePlayer.posZ;
                packetOffset(0.15);
                PlayerUtils.cancelFlag(x, y, z, true);
                packetOffset(0.025);
                x = mc.thePlayer.posX;
                y = mc.thePlayer.posY;
                z = mc.thePlayer.posZ;
                packetOffset(0.035);
                PlayerUtils.cancelFlag(x, y, z, true);
                packetOffset(0.032);
                packetOffset(0.035);
                x = mc.thePlayer.posX;
                y = mc.thePlayer.posY;
                z = mc.thePlayer.posZ;
                PlayerUtils.sendOffset(0, -0.1, 0);
                PlayerUtils.cancelFlag(x, y, z, true);
                PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
            } else {
                PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                double x = mc.thePlayer.posX;
                double y = mc.thePlayer.posY;
                double z = mc.thePlayer.posZ;
                packetOffset(0.07);
                PlayerUtils.cancelFlag(x, y, z, true);
                packetOffset(1E-4);
                packetOffset(0.15);
                x = mc.thePlayer.posX;
                y = mc.thePlayer.posY;
                z = mc.thePlayer.posZ;
                packetOffset(0.4);
                PlayerUtils.cancelFlag(x, y, z, true);
                PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
                packetOffset(getBaseMoveSpeed());
                mc.timer.timerSpeed = 0.5f;
            }
            canPhase = false;
        }
        super.onPost(event);
    }

    @Override
    public void onBB(EventBoundingBox event) {
        if ((getParent().passable.getValue() ? mc.gameSettings.keyBindSprint.pressed : isInBlock()) && event.getBoundingBox() != null && event.getBoundingBox().maxY > mc.thePlayer.boundingBox.minY) {
            event.setBoundingBox(null);
        }
    }

    private void packetOffset(double speed) {
        double[] h = calculate(speed);
        PlayerUtils.sendOffset(h[0], 0, h[1]);
        mc.thePlayer.expandPos(h[0], 0, h[1]);
    }
    private void packetOffsetForce(double speed) {
        double[] h = calculate2(speed, mc.thePlayer.rotationYaw, 1f);
        PlayerUtils.sendOffset(h[0], 0, h[1]);
        mc.thePlayer.expandPos(h[0], 0, h[1]);
    }

    @Override
    public void onSetBack(EventSetBack event) {
        super.onSetBack(event);
    }
}
