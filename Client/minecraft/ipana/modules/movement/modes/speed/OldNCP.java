package ipana.modules.movement.modes.speed;

import ipana.events.*;
import ipana.managements.module.Modules;
import ipana.modules.movement.Speed;
import ipana.utils.math.MathUtils;
import ipana.utils.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Collections;

import static ipana.utils.player.PlayerUtils.*;

public class OldNCP extends SpeedMode {
    public OldNCP(Speed parent) {
        super("OldNCP", parent);
    }

    private int flagTicks;
    private int wtf;
    private boolean stopOffset;
    private float lastYaw, lastPitch;
    private EventPreUpdate pre;

    @Override
    public void onEnable() {
        if (mc.thePlayer != null) {
            getParent().spd = getBaseMoveSpeed();
        }
        getParent().za = 0;
        getParent().ticks = 0;
        flagTicks = 0;
        getParent().spd = 0;
        wtf = 0;
        super.onEnable();
    }

    @Override
    public void onMoving(EventMoving event) {
        Speed speed = getParent();


        double hAllowedDistance = PlayerUtils.baseSpeed311(event.getX(), event.getZ());
        double someThreshold = hAllowedDistance / 3.3;

        double[] speeds = new double[]{
                hAllowedDistance * 2 + someThreshold,
                Math.max(hAllowedDistance + someThreshold, Math.min(hAllowedDistance + ncpListener().sfHorizontalBuffer, hAllowedDistance * 2 - someThreshold - 2E-7)),
                hAllowedDistance * 2 - 1E-7};

        if (speed.ticks >= 0 && speed.ticks < speeds.length) {
            speed.spd = speeds[speed.ticks];
        }

        boolean flagged = flagTicks > 0;
        if (flagged) {
            speed.spd = hAllowedDistance;
            speed.ticks = 2173;
            flagTicks--;
        }

        if (PlayerUtils.isInLiquid()) {
            speed.spd = hAllowedDistance;
        }

        double[] d = calculate(speed.spd);
        double[] atam = calculate(hAllowedDistance);

        /* STEP CHECK */
        AxisAlignedBB stepBB = PlayerUtils.getCollision2(d[0], 0, d[1]);
        if (stepBB != null) {
            d = atam;
            stopOffset = true;
            speed.ticks = 1;
            /*double collideX = (stepBB.minX + stepBB.maxX) / 2;
            double collideZ = (stepBB.minZ + stepBB.maxZ) / 2;
            double collideDist = Math.hypot(mc.thePlayer.posX-collideX, mc.thePlayer.posZ-collideZ);
            double nextH = collideDist - 1E-3;
            if (collideDist <= hAllowedDistance || speed.ticks == 1 || nextH - speed.lastDist < someThreshold) {
                d = atam;
                stopOffset = true;
                speed.ticks = 1;
            } else {
                d = calculate(nextH);
            }*/
        }

        /* UNDER BLOCK CHECK */
        if (mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0,-0.0625,0)).isEmpty()) {
            speed.spd = speed.lastDist - speed.lastDist / 130 - 1E-7;
            speed.spd = Math.min(speed.spd, hAllowedDistance + someThreshold);
            d = calculate(speed.spd);
            stopOffset = true;
            speed.ticks = 2173;
        }
        event.setX(d[0]);
        event.setZ(d[1]);
        super.onMoving(event);
    }

    @Override
    public void onStep(EventStep event) {

        super.onStep(event);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        Speed speed = getParent();

        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        speed.lastDist = Math.hypot(xDist, zDist);

        boolean isOnGround = !mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0,-0.0625,0)).isEmpty();
        if (isMoving2() && isOnGround && speed.ticks >= 0 && speed.ticks < 3 && !stopOffset) {
            event.setY(event.getY() + 0.02173 + (speed.ticks + 1) * 1E-6);
        }

        stopOffset = false;
        speed.timerBoost();
        pre = event;
        super.onPre(event);
    }

    @Override
    public void onPost(EventPostUpdate event) {
        Speed speed = getParent();
        if (!pre.isCancelPackets() && isMoving2() && ++speed.ticks >= 3) {
            speed.ticks = 0;
        }
        super.onPost(event);
    }

    @Override
    public double nextSpeed() {
        Speed speed = getParent();
        double hAllowedDistance = PlayerUtils.baseSpeed311(0, 0);
        double someThreshold = hAllowedDistance / 3.3;

        double[] speeds = new double[]{
                hAllowedDistance*2+someThreshold,
                hAllowedDistance+someThreshold,
                hAllowedDistance*2-1E-7};

        if (speed.ticks >= 0 && speed.ticks < speeds.length) {
            return speeds[speed.ticks];
        }
        return super.nextSpeed();
    }

    @Override
    public double nextY() {
        Speed speed = getParent();
        double nextY = super.nextY();
        if (speed.ticks >= 0 && speed.ticks < 3) {
            nextY = 0.02173 + (speed.ticks + 1) * 1E-6;
        }
        if (++speed.ticks >= 3) {
            speed.ticks = 0;
        }
        return nextY;
    }

    @Override
    public void onReceive(EventPacketReceive event) {
        if (event.getState() == EventPacketReceive.PacketState.POST && event.getPacket() instanceof S08PacketPlayerPosLook) {
            //Well we got flagged somehow.
            //There is no way of using default hAllowedSpeed after we flagged.
            //That way you should go with speeds lower than hAllowed even with that you need go for at least 3 ticks for flawless experience.
            //However, gliding down resets speed ticks.
            //For example if we flagged at tick 1 (our last move was 0.667) flagY will be 0.021731.
            //On the next tick we will go with hAllowedSpeed and Y of 0.021732 by offsetting a little bit up.
            //Then gliding down to original Y of 0.021731 and setting tick to 1
            //An useless looking code does some insane job on flag recovery!
            mc.thePlayer.expandPos(0, 1E-6, 0);
            flagTicks = 1;
        }
        super.onReceive(event);
    }
}