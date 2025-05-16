package ipana.modules.movement.modes.speed;

import ipana.events.*;
import ipana.modules.movement.Speed;
import ipana.utils.math.MathUtils;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;

import static ipana.utils.player.PlayerUtils.*;

public class Fantasy extends SpeedMode {
    public Fantasy(Speed parent) {
        super("Fantasy", parent);
    }
    private int wtf;
    private boolean slowMode;
    private boolean falling;
    private int flagTicks;

    @Override
    public void onEnable() {
        if (mc.thePlayer != null) {
            getParent().spd = getBaseMoveSpeed();
        }
        getParent().za = 0;
        getParent().lastDist = 0;
        getParent().ticks = 0;
        super.onEnable();
    }

    @Override
    public void onMoving(EventMoving event) {
        Speed speed = getParent();
        if (mc.thePlayer.isSneaking() || !PlayerUtils.isMoving2() || mc.gameSettings.keyBindJump.pressed) {
            mc.timer.timerSpeed = 1.0F;
            return;
        }
        if (!PlayerUtils.wasMoving()) {
            double[] d = calculate(getBaseMoveSpeed(0.29, true));
            event.setX(d[0]);
            event.setZ(d[1]);
            return;
        }
        double hAllowedDistance = getBaseMoveSpeed(0.29, true);
        double someThreshold = hAllowedDistance / 3.3;

        if (speed.ticks == 0) {
            speed.spd = hAllowedDistance * 2 + someThreshold;
        } else if (speed.ticks == 1) {
            speed.spd = (hAllowedDistance + someThreshold);
        } else if (speed.ticks == 4) {
            speed.spd = hAllowedDistance * 2 + someThreshold - 0.1105 - 1E-5;
        }  else if (speed.ticks >= 2) {
            speed.spd = Math.min(hAllowedDistance+someThreshold, speed.lastDist - speed.lastDist / 130 - 1E-5);
        }
        if (flagTicks-- > 0) {
            speed.spd = 0.07;
            speed.ticks = -1;
        }
        double[] d = calculate(speed.spd);

        falling = false;
        /* STEP CHECK */
        AxisAlignedBB stepBB = PlayerUtils.getCollision(d[0], 0, d[1]);
        if (stepBB != null) {
            falling = true;
            d = calculate(hAllowedDistance);
            speed.ticks = 2173;
        }

        /* UNDER BLOCK CHECK */
        if (mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(d[0], -0.0625, d[1])).isEmpty()) {
            falling = true;
            d = calculate(hAllowedDistance);
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
        if (mc.thePlayer.isSneaking() || !PlayerUtils.isMoving2() || mc.gameSettings.keyBindJump.pressed) {
            mc.timer.timerSpeed = 1.0F;
            return;
        }
        speed.timerBoost();
        if (!falling) {
            event.setY(event.getY() + 1E-8);
            if (speed.ticks == 0 || speed.ticks == 2) {
                List<AxisAlignedBB> headCollision = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0, getOffset(), 0));
                slowMode = !headCollision.isEmpty() || !speed.korkunc.getValue();
                event.setY(event.getY() + (slowMode ? 0.07 : getOffset()));
                event.setOnGround(true);
            }
        }
        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        speed.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
        super.onPre(event);
    }

    @Override
    public void onPost(EventPostUpdate event) {
        Speed speed = getParent();
        speed.ticks++;
        if (speed.ticks >= (slowMode ? 4 : 5)) {
            speed.ticks = 0;
        }
        super.onPost(event);
    }
    @Override
    public void onReceive(EventPacketReceive event) {
        if (event.getState() == EventPacketReceive.PacketState.PRE && event.getPacket() instanceof S08PacketPlayerPosLook pos) {
            flagTicks = 3;
        }
        super.onReceive(event);
    }
    private double getOffset() {
        //for slabs it should be more than 0.5625
        //for carpets it should be more than 1.0
        double y = 1.001;
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            y+=mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier()+0.59;
        }
        return y;
    }
}
