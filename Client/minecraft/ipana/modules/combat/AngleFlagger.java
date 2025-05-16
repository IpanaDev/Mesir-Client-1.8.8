package ipana.modules.combat;

import ipana.events.*;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.NumberValue;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class AngleFlagger extends Module {
    private NumberValue<Integer> hurtTime = new NumberValue<>("Hurt", this, 14, 10, 20, 1, "dj hakan keles life");
    private NumberValue<Integer> move = new NumberValue<>("MoveTime", this, 10, 1, 20, 1, "oynaniyo final");
    private BoolValue blink = new BoolValue("Blink", this, false, "Blink around");

    public AngleFlagger() {
        super("AngleFlagger", Keyboard.KEY_NONE, Category.Combat, "kayiyomu.");
    }

    private List<Packet> packets = new ArrayList<>();
    private boolean listenPackets = true;

    private HashMap<EntityLivingBase, TargetData> data = new HashMap<>();
    private int moveTicks;
    private int waitTicks;
    private float startYaw;
    private float lastYawDiff;
    private boolean incrementWaitTime;

    private Listener<EventMoving> onMove = new Listener<EventMoving>(event -> {
        KillAura ka = Modules.KILL_AURA;
        boolean kaCheck = ka.isEnabled() && !ka.targets.isEmpty() && ka.curTar != null;
        if (!kaCheck)
            return;

        if (blink.getValue()) {
            if (mc.thePlayer.hurtResistantTime == hurtTime.getValue()) {
                listenPackets = false;
                for (var packet : packets) {
                    PlayerUtils.packet(packet);
                }
                listenPackets = true;
                packets.clear();
                moveTicks = moveTicks == 0 ? 1 : 0;
                if (moveTicks == 0) {
                    incrementWaitTime ^= true;
                }
            }
            if (moveTicks == 0) {
                event.setX(0);
                event.setZ(0);
            } else {
                mc.thePlayer.movementInput.moveForward = 1f;
                mc.thePlayer.movementInput.moveStrafe = incrementWaitTime ? 1f : -1f;
                double[] c = PlayerUtils.calculate(PlayerUtils.getBaseMoveSpeed(), RotationUtils.getRotations(ka.curTar)[0], 90 / mc.thePlayer.getDistanceToEntity(ka.curTar));
                event.setX(mc.thePlayer.motionX = c[0]);
                event.setZ(mc.thePlayer.motionZ = c[1]);
            }
            return;
        }
        var targetData = data.get(ka.curTar);
        if (targetData == null)
            return;

        if (mc.thePlayer.hurtResistantTime <= 5) {
            incrementWaitTime = false;
            waitTicks = 0;
        }

        if (mc.thePlayer.hurtResistantTime == hurtTime.getValue() && waitTicks-- <= 0) {
            moveTicks = move.getValue();
            waitTicks = incrementWaitTime ? 2 : 1;
            //incrementWaitTime ^= true;
            float multiplier = Math.max(1050 - targetData.lastActivity(), 0) / 1000f;
            if (multiplier <= 0) {
                multiplier = 1;
            }
            float yawToTarget = RotationUtils.getRotations(ka.curTar)[0];
            float yawDiff = yawDiff(yawToTarget, targetData) * multiplier;
            startYaw = yawToTarget + yawDiff;
            PlayerUtils.debug("MODE1: " + targetData.lastActivity() + " : " + yawDiff + " : " + targetData.movingTicks);
        }
        if (moveTicks-- > 0 && !PlayerUtils.isMoving2()) {
            double[] c = PlayerUtils.calculate2(PlayerUtils.getBaseMoveSpeed(), startYaw, 1);
            event.setX(mc.thePlayer.motionX = c[0]);
            event.setZ(mc.thePlayer.motionZ = c[1]);
        }
    }).weight(Event.Weight.MONITOR);

    private Listener<EventPreUpdate> onPre = new Listener<>(event -> {
        KillAura ka = Modules.KILL_AURA;
        boolean kaCheck = ka.isEnabled() && !ka.targets.isEmpty() && ka.curTar != null;
        if (!kaCheck) {
            return;
        }
        data.entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue().activityMs >= 60000);
        data.putIfAbsent(ka.curTar, new TargetData(ka.curTar));
        var targetData = data.get(ka.curTar);
        targetData.update();
    });

    private Listener<EventPostUpdate> onPost = new Listener<>(event -> {

    });

    private Listener<EventPacketSend> onSend = new Listener<>(event -> {
        if (blink.getValue() && listenPackets && event.getPacket() instanceof C03PacketPlayer) {
            packets.add(event.getPacket());
            event.setCancelled(true);
        }
    });

    private float yawDiff(float yawToTarget, TargetData targetData) {
        float yawDiff = 45;
        float distYaw0 = RotationUtils.getDistanceBetweenAngles(yawToTarget + yawDiff, targetData.moveAngle);
        float distYaw1 = RotationUtils.getDistanceBetweenAngles(yawToTarget - yawDiff, targetData.moveAngle);
        yawDiff *= distYaw1 > distYaw0 ? -1 : 1;
        if (targetData.directionMismatch) {
            //PlayerUtils.debug(distYaw0+", "+distYaw1+", "+(System.currentTimeMillis()-targetData.activityMs));
            yawDiff *= -1;
            targetData.directionMismatch = false;
        }
        return yawDiff;
    }

    @Override
    public void onEnable() {
        super.onEnable();
    }

    private static class TargetData {
        EntityLivingBase entity;
        long activityMs;
        boolean stopped;
        boolean directionMismatch;
        int stoppingTicks;
        int movingTicks;
        Vec3 stopPosition;
        float moveAngle;

        public TargetData(EntityLivingBase entity) {
            this.entity = entity;
            markActive();
        }

        public void update() {
            double xDiff = entity.posX - entity.lastTickPosX;
            double zDiff = entity.posZ - entity.lastTickPosZ;
            //Should we get yDiff too??
            double speed = Math.hypot(xDiff, zDiff);
            if (speed <= 0.0625) {
                movingTicks = 0;
                if (!stopped && stoppingTicks++ >= 4) {
                    //PlayerUtils.debug(entity.getName()+" stopped.");
                    var newPosition = new Vec3(entity.posX, entity.posY, entity.posZ);
                    if (stopPosition != null) {
                        double moveX = newPosition.xCoord - stopPosition.xCoord;
                        double moveZ = newPosition.zCoord - stopPosition.zCoord;
                        moveAngle = (float) ((Math.atan2(moveZ, moveX) * 180.0D / (Math.PI)) - 90.0F) - 180;
                        //PlayerUtils.debug(entity.getName() + "'s move angle is " + moveAngle);
                    }
                    stopPosition = newPosition;
                    stopped = true;
                }
            } else {
                markActive();
                if (stopped) {
                    var newPosition = new Vec3(entity.posX, entity.posY, entity.posZ);
                    double moveX = newPosition.xCoord - stopPosition.xCoord;
                    double moveZ = newPosition.zCoord - stopPosition.zCoord;
                    float realAngle = (float) ((Math.atan2(moveZ, moveX) * 180.0D / (Math.PI)) - 90.0F);
                    float distAngles = RotationUtils.getDistanceBetweenAngles(realAngle, moveAngle);
                    if (distAngles > 45) {
                        directionMismatch = true;
                    }
                    stopped = false;
                    stoppingTicks = 0;
                }
                movingTicks++;
            }
        }

        public void markActive() {
            activityMs = System.currentTimeMillis();
        }

        public long lastActivity() {
            return System.currentTimeMillis() - activityMs;
        }
    }
}
