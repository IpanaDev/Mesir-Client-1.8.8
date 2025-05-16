package ipana.modules.movement.modes.speed;

import ipana.events.EventMoveInput;
import ipana.events.EventMoving;
import ipana.events.EventPacketReceive;
import ipana.events.EventPreUpdate;
import ipana.modules.movement.Speed;
import ipana.utils.player.PlayerUtils;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

import static ipana.utils.player.PlayerUtils.calculate;
import static ipana.utils.player.PlayerUtils.getBaseMoveSpeed;

public class Hop3111  extends SpeedMode {
    public Hop3111(Speed parent) {
        super("Surf", parent);
    }
    private int flagTicks;

    @Override
    public void onMoving(EventMoving event) {
        Speed speed = getParent();
        double hAllowedDistance = getBaseMoveSpeed(0.29, true);
        double someThreshold = hAllowedDistance / 3.3;
        if (mc.thePlayer.onGround) {
            if (speed.ticks > 0) {
                speed.spd = hAllowedDistance*2+someThreshold-0.1105-1E-6;
                speed.ticks = 0;
            } else {
                speed.spd = hAllowedDistance*2+someThreshold;
                event.setY(mc.thePlayer.motionY = 0.42);
            }
        } else {
            if (event.getY() < 0 && event.getY() > -0.0625) {
                event.setY(mc.thePlayer.motionY = -0.0625);
            }
            if (speed.ticks == 0) {
                speed.spd = hAllowedDistance+someThreshold;
                event.setY(mc.thePlayer.motionY -= 0.05);
            } else {
                speed.spd = speed.lastDist - speed.lastDist/130 - 1E-6;
                if (speed.spd > hAllowedDistance+someThreshold) {
                    speed.spd = hAllowedDistance+someThreshold;
                }
            }
            speed.ticks++;
        }
        if (flagTicks-- > 0) {
            speed.spd = 0.063;
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
            flagTicks = 1;
        }
        super.onReceive(event);
    }
}
