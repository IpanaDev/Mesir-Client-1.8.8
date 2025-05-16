package ipana.modules.movement.modes.speed;

import ipana.events.EventMoving;
import ipana.events.EventPacketReceive;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.modules.movement.Speed;
import ipana.utils.math.MathUtils;
import ipana.utils.player.PlayerUtils;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;

import java.util.List;

import static ipana.utils.player.PlayerUtils.*;

public class LatestNCPHop extends SpeedMode {
    public LatestNCPHop(Speed parent) {
        super("LatestNCPHop", parent);
    }
    private boolean flagged;
    @Override
    public void onEnable() {
        Speed speed = getParent();
        speed.ticks = -1;
        speed.spd = getBaseMoveSpeed();
        super.onEnable();
    }

    @Override
    public void onMoving(EventMoving event) {
        Speed speed = getParent();
        double base = getBaseMoveSpeed();
        if (mc.thePlayer.onGround) {
            if (speed.ticks == -1) {
                speed.spd = base;
            } else {
                mc.thePlayer.motionY = 0.4;
                speed.spd = speed.lastDist * (1.546 - Math.max(0, 0.025 * (10-speed.ticks)));
            }
            speed.ticks = 0;
        } else {
            if (speed.ticks == 0) {
                double difference = 0.66 * (speed.lastDist - base);
                speed.spd = speed.lastDist - difference;
            } else {
                speed.spd = speed.lastDist - speed.lastDist / 159;
            }
            speed.ticks++;
        }
        event.setY(mc.thePlayer.motionY);
        if (mc.thePlayer.isRiding()) {
            speed.spd = base;
        }
        speed.spd = Math.max(speed.spd, base);
        if (flagged) {
            speed.spd = 0.09;
            flagged = false;
        }
        double[] d = calculate(speed.spd);
        event.setX(d[0]);
        event.setZ(d[1]);
        super.onMoving(event);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        Speed speed = getParent();
        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        speed.lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
        speed.timerBoost();
        super.onPre(event);
    }

    @Override
    public void onReceive(EventPacketReceive event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            flagged = true;
        }
        super.onReceive(event);
    }
}
