package ipana.modules.movement.modes.phase;


import ipana.events.*;
import ipana.modules.movement.Phase;
import ipana.utils.player.PlayerUtils;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.MathHelper;

import static ipana.utils.player.PlayerUtils.calculate;
import static ipana.utils.player.PlayerUtils.isInBlock;

public class EfsaneCraft extends PhaseMode {
    public EfsaneCraft(Phase parent) {
        super("EfsaneCraft", parent);
    }
    private boolean canPhase;
    private int stage;
    private float yaw, pitch;

    @Override
    public void onEnable() {
        stage = 0;
        super.onEnable();
    }

    @Override
    public void onMove(EventMoving event) {
        if (stage > 0) {
            event.setX(0);
            event.setY(0);
            event.setZ(0);
        }
    }
    @Override
    public void onPre(EventPreUpdate event) {
        mc.thePlayer.noClip = true;
        if (stage > 0) {
            event.setCancelPackets(true);
        }
        if (!isInBlock() && mc.thePlayer.isCollidedHorizontally && PlayerUtils.isMoving2() && stage == 0) {
            float off = 45;
            event.setPitch(event.getPitch() + off);
            if (event.getPitch() > 90) {
                event.setPitch(-off);
            }
            yaw = MathHelper.wrapAngleTo180_float(event.getYaw());
            pitch = event.getPitch();
            canPhase = true;
        }
    }

    @Override
    public void onPost(EventPostUpdate event) {
        mc.timer.timerSpeed = 1f;
        if (canPhase && stage == 0) {
            PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
            packetOffset(0.005);
            PlayerUtils.debug("STAGE 1 PREDICT: "+mc.thePlayer.posX+", "+mc.thePlayer.posZ);
            packetOffset(0.15);
            stage = 1;
            canPhase = false;
        }
        super.onPost(event);
    }

    @Override
    public void onBB(EventBoundingBox event) {
        if (mc.gameSettings.keyBindSprint.pressed && event.getBoundingBox() != null && event.getBoundingBox().maxY > mc.thePlayer.boundingBox.minY) {
            event.setBoundingBox(null);
        }
    }

    private void packetOffset(double speed) {
        double[] h = calculate(speed);
        mc.thePlayer.expandPos(h[0], 0, h[1]);
        PlayerUtils.send(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, yaw, pitch, false);
    }

    @Override
    public void onSetBack(EventSetBack event) {
        if (stage == 0) {
            return;
        }
        switch (event.state()) {
            case PRE -> event.cancelPacket(true);
            case POST -> {
                if (yaw != event.yaw() || pitch != event.pitch()) {
                    PlayerUtils.debug("Incorrect Rotations: "+(yaw - event.yaw())+", "+(pitch - event.pitch()));
                }
                switch (stage) {
                    case 1 -> {
                        PlayerUtils.send(event.xOff(), event.yOff(), event.zOff(), event.yaw(), event.pitch(), false);
                        PlayerUtils.debug("STAGE 1 FLAG:      "+event.xOff()+", "+event.zOff());
                        packetOffset(0.025);
                        PlayerUtils.debug("STAGE 2 PREDICT: "+mc.thePlayer.posX+", "+mc.thePlayer.posZ);
                        packetOffset(0.035);
                        stage = 2;
                    }
                    case 2 -> {
                        PlayerUtils.send(event.xOff(), event.yOff(), event.zOff(), event.yaw(), event.pitch(), false);
                        PlayerUtils.debug("STAGE 2 FLAG:      "+event.xOff()+", "+event.zOff());
                        packetOffset(0.032);
                        packetOffset(0.035);
                        PlayerUtils.sendOffset(0, -0.1, 0);
                        PlayerUtils.debug("STAGE 3 PREDICT: "+mc.thePlayer.posX+", "+mc.thePlayer.posZ);
                        stage = 3;
                    }
                    case 3 -> {
                        PlayerUtils.send(event.xOff(), event.yOff(), event.zOff(), event.yaw(), event.pitch(), false);
                        PlayerUtils.debug("STAGE 3 FLAG:      "+event.xOff()+", "+event.zOff());
                        PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
                        stage = 0;
                    }
                }
            }
        }
        super.onSetBack(event);
    }
}
