package ipana.modules.movement.modes.speed;

import ipana.events.EventFrame;
import ipana.events.EventMoving;
import ipana.events.EventPacketReceive;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.modules.movement.Speed;
import ipana.utils.math.MathUtils;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockPackedIce;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;

import java.util.List;

import static ipana.utils.player.PlayerUtils.*;

public class OldNCPHop extends SpeedMode {
    public OldNCPHop(Speed parent) {
        super("OldNCPHop", parent);
    }
    private boolean firstBoost;

    @Override
    public void onEnable() {
        firstBoost = true;
        super.onEnable();
    }

    @Override
    public void onMoving(EventMoving event) {
        Speed speed = getParent();

        if (speed.ticks == 7) {
            event.setY(mc.thePlayer.motionY = -0.185);
        } else if (speed.ticks == 8) {
            event.setY(mc.thePlayer.motionY = -0.185);
        } else if (speed.ticks == 9) {
            event.setY(mc.thePlayer.motionY = -0.36);
        }
        if (mc.thePlayer.moveForward == 0.0F && mc.thePlayer.moveStrafing == 0.0F) {
            speed.spd = getBaseMoveSpeed();
            speed.za = 0;
            speed.boost = false;
        }
        if ((speed.ticks == 1) && (mc.thePlayer.isCollidedVertically) && ((mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F))) {
            speed.spd = getBaseMoveSpeed()+ncpListener().sfHorizontalBuffer;
        } else if ((speed.ticks == 2) && (mc.thePlayer.isCollidedVertically) && ((mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F))) {
            double offset = 0.4;
            if (mc.thePlayer.isPotionActive(Potion.jump)) {
                offset += ((mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.11F);
            }
            event.setY(mc.thePlayer.motionY = offset);
            speed.spd *= 2.15-1E-7;
            firstBoost = false;
        } else if (speed.ticks == 3) {
            double difference = 0.66D * (speed.lastDist - getBaseMoveSpeed());
            speed.spd = (speed.lastDist - difference);
        } else {
            List<AxisAlignedBB> collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0D, mc.thePlayer.motionY, 0.0D));
            if ((collidingList.size() > 0) || (mc.thePlayer.isCollidedVertically)) {
                if (speed.ticks > 0) {
                    if (1.35D * getBaseMoveSpeed() - 0.01D > speed.spd) {
                        speed.ticks = 0;
                    } else {
                        speed.ticks = (mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F) ? 1 : 0;
                    }
                    speed.boost = !speed.boost;
                }
            }
            speed.spd = (speed.lastDist - speed.lastDist / (160 - 1E-7));
        }

        double atat = Math.max(speed.spd, getBaseMoveSpeed());
        double[] c = calculate(atat);
        event.setX(c[0]);
        event.setZ(c[1]);


        if ((mc.thePlayer.moveForward != 0.0F) || (mc.thePlayer.moveStrafing != 0.0F)) {
            speed.ticks += 1;
        }

        super.onMoving(event);
    }
    @Override
    public void onReceive(EventPacketReceive event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            getParent().ticks = -3;
        }
        super.onReceive(event);
    }

    @Override
    public void onFrame(EventFrame event) {

        super.onFrame(event);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        Speed speed = getParent();
        if (!PlayerUtils.isMoving2()) {
            return;
        }
        speed.timerBoost();
        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;

        speed.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);

        super.onPre(event);
    }
}
