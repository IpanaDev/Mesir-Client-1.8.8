package ipana.modules.player.strategy;

import ipana.events.EventMoveInput;
import ipana.events.EventMoving;
import ipana.events.EventPacketSend;
import ipana.events.EventPostUpdate;
import ipana.managements.module.Modules;
import ipana.modules.combat.KillAura;
import ipana.modules.player.AutoDrink;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;

public class MoveStrategy {
    private ArrayList<Position> positions = new ArrayList<>();
    private Minecraft mc = Minecraft.getMinecraft();
    private AutoDrink autoDrink;
    public int blinkTicks;
    public int runTicks;

    public MoveStrategy(AutoDrink autoDrink) {
        this.autoDrink = autoDrink;
    }

    public void onMove(EventMoving event) {
        if (runTicks > 0) {
            double hypot = Math.hypot(event.getX(), event.getZ());
            float safeYaw = getSafestDirection();
            double[] c = PlayerUtils.calculate2(hypot, safeYaw, 1);
            AxisAlignedBB bb = PlayerUtils.getCollision(c[0], 0, c[1]);
            if (bb != null) {
                c = PlayerUtils.calculate2(hypot, getSafestDirection(10), 1);
                /*double diffX = (bb.minX + bb.maxX) / 2 - mc.thePlayer.posX;
                double diffZ = (bb.minZ + bb.maxZ) / 2 - mc.thePlayer.posZ;
                double absX = Math.abs(diffX);
                double absZ = Math.abs(diffZ);
                //An if check can be added absX and absZ are less than 0.0625, so a block check safest direction can be searched (path set, so it doesn't break next tick)
                if (absX > absZ) {
                    event.setX(hypot * (diffX / absX));
                    event.setZ(0);
                } else if (absZ > absX) {
                    event.setX(0);
                    event.setZ(hypot * (diffZ / absZ));
                }*/
            }
            event.setX(c[0]);
            event.setZ(c[1]);
        }
    }

    public void onInput(EventMoveInput event) {
        KillAura ka = Modules.KILL_AURA;
        boolean hasTarget = !ka.targets.isEmpty() && ka.curTar != null;
        if (runTicks > 0 || blinkTicks > 0 || hasTarget && Modules.AUTO_DRINK.drinkStrat.getValue() && Modules.AUTO_DRINK.moveStrat.getValue() && Modules.AUTO_TARGET.isEnabled()) {
            event.setForward(mc.thePlayer.movementInput.moveForward = 0.98f);
        }
    }

    public void onPost(EventPostUpdate event) {
        if (runTicks > 0) {
            runTicks--;
        }
        if (blinkTicks > 0) {
            blinkTicks--;
            if (blinkTicks <= 0) {
                for (Position pos : positions) {
                    switch (pos.type) {
                        case C03 -> PlayerUtils.packet(new C03PacketPlayer(pos.ground));
                        case C04 -> PlayerUtils.packet(new C03PacketPlayer.C04PacketPlayerPosition(pos.x, pos.y, pos.z, pos.ground));
                        case C05 -> PlayerUtils.packet(new C03PacketPlayer.C05PacketPlayerLook(pos.yaw, pos.pitch, pos.ground));
                        case C06 -> PlayerUtils.packet(new C03PacketPlayer.C06PacketPlayerPosLook(pos.x, pos.y, pos.z, pos.yaw, pos.pitch, pos.ground));
                    }
                }
                positions.clear();
            }
        }
    }

    public void onSend(EventPacketSend event) {
        if (event.getPacket() instanceof C03PacketPlayer || event.getPacket() instanceof C03PacketPlayer.C05PacketPlayerLook || event.getPacket() instanceof C03PacketPlayer.C04PacketPlayerPosition || event.getPacket() instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
            if (blinkTicks > 0) {
                switch (event.getPacket()) {
                    case C03PacketPlayer.C06PacketPlayerPosLook packet -> positions.add(new Position(packet.x, packet.y, packet.z, packet.yaw, packet.pitch, packet.onGround));
                    case C03PacketPlayer.C05PacketPlayerLook packet -> positions.add(new Position(packet.yaw, packet.pitch, packet.onGround));
                    case C03PacketPlayer.C04PacketPlayerPosition packet -> positions.add(new Position(packet.x, packet.y, packet.z, packet.onGround));
                    case null, default -> {
                        C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();
                        positions.add(new Position(packet.onGround));
                    }
                }
                event.setCancelled(true);
            }
        }
    }

    private float getSafestDirection() {
        //TODO: is safest direction walkable?
        KillAura ka = Modules.KILL_AURA;
        float sumYaw = 0;
        for (EntityLivingBase entity : ka.targets) {
            sumYaw += RotationUtils.getRotations(entity)[0] % 360f;
        }
        return sumYaw / Math.max(ka.targets.size(), 1) - 180;
    }
    private float getSafestDirection(double hDist) {
        //TODO: is safest direction walkable?
        double dist = 0;
        float safeYaw = 0;
        KillAura ka = Modules.KILL_AURA;
        double sumPosX = 0;
        double sumPosZ = 0;
        int targets = Math.max(ka.targets.size(), 1);
        for (EntityLivingBase entity : ka.targets) {
            sumPosX += entity.posX;
            sumPosZ += entity.posZ;
        }
        double dangerX = sumPosX / targets;
        double dangerZ = sumPosZ / targets;
        for (int i = 0; i < 360; i+=5) {
            double[] calc = PlayerUtils.calculate2(hDist, i, 1);
            AxisAlignedBB bb = PlayerUtils.getCollision(calc[0], 0, calc[1], true);
            double predictedX, predictedZ;
            if (bb != null) {
                predictedX = (bb.minX + bb.maxX) / 2;
                predictedZ = (bb.minZ + bb.maxZ) / 2;
            } else {
                predictedX = mc.thePlayer.posX + calc[0];
                predictedZ = mc.thePlayer.posZ + calc[1];
            }
            double diffX = dangerX - predictedX;
            double diffZ = dangerZ - predictedZ;
            double predictDist = diffX * diffX + diffZ * diffZ;
            if (dist < predictDist) {
                dist = predictDist;
                safeYaw = i;
            }
        }
        return safeYaw;
    }
    class Position {
        double x,y,z;
        float yaw,pitch;
        boolean ground;
        Type type;

        public Position(double x, double y, double z, float yaw, float pitch, boolean ground) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
            this.ground = ground;
            this.type = Type.C06;
        }

        public Position(float yaw, float pitch, boolean ground) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.ground = ground;
            this.type = Type.C05;
        }

        public Position(double x, double y, double z, boolean ground) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.ground = ground;
            this.type = Type.C04;
        }

        public Position(boolean ground) {
            this.ground = ground;
            this.type = Type.C03;
        }
    }

    enum Type {
        C03, C04, C05, C06
    }
}
