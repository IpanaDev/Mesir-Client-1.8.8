package ipana.modules.movement.modes.fly;

import ipana.events.EventMoving;
import ipana.events.EventPacketReceive;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.modules.movement.Fly;
import ipana.utils.player.PlayerUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

import static ipana.utils.player.PlayerUtils.calculate;
import static ipana.utils.player.PlayerUtils.getBaseMoveSpeed;

public class Vanilla extends FlyMode {
    public Vanilla(Fly parent) {
        super("Vanilla", parent);
    }
    private Fly fly = getParent();
    private int ticks;
    private double movingSpeed;
    private double lastDist;

    @Override
    public void onEnable() {
        ticks = 0;
        if (fly.latest.getValue()) {
            mc.thePlayer.jump();
        }
        super.onEnable();
    }

    @Override
    public void onMove(EventMoving event) {
        if (ticks == 0) {
            movingSpeed *= 2.8;
            ticks = 1;
        } else if (ticks == 1) {
            double difference = 0.1 * (lastDist - PlayerUtils.getBaseMoveSpeed());
            movingSpeed = lastDist - difference;
            ticks = 0;
        }
        movingSpeed = Math.min(movingSpeed, 9.99);
        double[] c = calculate(fly.latest.getValue() ? movingSpeed : fly.speed.getValue());
        event.setX(c[0]);
        event.setZ(c[1]);
        super.onMove(event);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        mc.thePlayer.onGround = true;
        if (ticks == 1 && fly.latest.getValue()) {
            event.setY(event.getY()+0.4);
        }
        double spd = fly.speed.getValue();
        //event.setYaw(event.getYaw()+(mc.thePlayer.ticksExisted%2==0?-0:80));
        if (mc.gameSettings.keyBindJump.pressed) {
            mc.thePlayer.motionY = spd;
        } else if (mc.gameSettings.keyBindSneak.pressed) {
            mc.thePlayer.motionY = -spd;
        } else {
            mc.thePlayer.motionY = 0;
        }
        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
        super.onPre(event);
    }

    @Override
    public void onReceive(EventPacketReceive event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            movingSpeed = getBaseMoveSpeed();
            ticks = 0;
        }
        super.onReceive(event);
    }
}
