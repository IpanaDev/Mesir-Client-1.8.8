package ipana.modules.movement.modes.fly;

import ipana.events.EventMoving;
import ipana.events.EventPacketReceive;
import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.modules.movement.Fly;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.MathHelper;

import static ipana.utils.player.PlayerUtils.calculate;

public class BowFly extends FlyMode {
    public BowFly(Fly parent) {
        super("BowFly", parent);
    }
    private double movingSpeed;
    private double ticks;

    @Override
    public void onPre(EventPreUpdate event) {
        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() != null) {
            float pitch = -90;
            if (mc.thePlayer.moveForward > 0) {
                double[] predict = PlayerUtils.calculate(movingSpeed, mc.thePlayer.rotationYaw, 60);
                double offsetY = mc.thePlayer.motionY < -0.08 ? 0 : 2;
                float[] rot = RotationUtils.getRotationFromPosition(mc.thePlayer.posX+predict[0], mc.thePlayer.posZ+predict[1], mc.thePlayer.posY+offsetY);
                pitch = rot[1];
            }
            event.setPitch(pitch);
            mc.thePlayer.rotationPitchHead=event.getPitch();
            mc.timer.timerSpeed = 0.2f;
            if (!PlayerUtils.isMoving2()) {
                movingSpeed = 0;
            }
            ticks--;
        }
        super.onPre(event);
    }

    @Override
    public void onPost(EventPostUpdate event) {
        if (ticks <= 0) {
            mc.playerController.onStoppedUsingItem(mc.thePlayer);
            PlayerUtils.packet(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
            ticks = 3;
            if (movingSpeed > 2.5) {
                ticks = 4;
            }
            if (movingSpeed > 4.5) {
                ticks = 5;
            }
        }
        super.onPost(event);
    }

    @Override
    public void onMove(EventMoving event) {
        double base = PlayerUtils.getBaseMoveSpeed();
        double freedom = PlayerUtils.getHFreedomNoCopy(9.99 - base);
        movingSpeed = Math.min(9.99, base + freedom);
        double[] c = PlayerUtils.calculate(movingSpeed);
        event.setX(mc.thePlayer.motionX = c[0]);
        event.setZ(mc.thePlayer.motionZ = c[1]);
        double vertical = PlayerUtils.getVFreedom(event.getY());
        if (vertical != -2173) {
            event.setY(mc.thePlayer.motionY = vertical + 0.0625);
        } else {
            event.setY(mc.thePlayer.motionY = event.getY() + 0.0159);
        }
        super.onMove(event);
    }

    @Override
    public void onEnable() {
        movingSpeed = 0;
        ticks = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.playerController.onStoppedUsingItem(mc.thePlayer);
        movingSpeed = 0;
        super.onDisable();
    }
}
