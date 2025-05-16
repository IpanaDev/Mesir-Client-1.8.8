package ipana.modules.movement.modes.fly;

import ipana.events.EventMoving;
import ipana.events.EventPreUpdate;
import ipana.modules.movement.Fly;
import ipana.utils.ncp.utilities.VelocityData;
import ipana.utils.player.PlayerUtils;
import net.minecraft.network.play.client.C03PacketPlayer;

public class Damage extends FlyMode {
    //-0.03126
    private boolean jumpListening;
    private int damageLag;
    private int damageCount;
    private double startY;
    private double verticalVelocity, verticalFreedom, prevFreedom;
    private int verticalVelocityUsed, verticalVelocityCounter;
    private int disableTicks;


    public Damage(Fly parent) {
        super("Damage", parent);
    }

    @Override
    public void onEnable() {
        jumpListening = true;
        damageCount = 0;
        verticalFreedom = 0;
        verticalVelocity = 0;
        disableTicks = 1;
        super.onEnable();
    }

    @Override
    public void onMove(EventMoving event) {
        if (jumpListening) {
            event.setX(0);
            event.setZ(0);
        } else {
            double[] calc = PlayerUtils.calculate(PlayerUtils.getBaseMoveSpeed(0.29, true));
            event.setX(calc[0]);
            event.setZ(calc[1]);
        }
        super.onMove(event);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        if (disableTicks > 0) {
            event.setCancelPackets(true);
            disableTicks--;
            return;
        }
        if (jumpListening) {
            mc.thePlayer.jumpTicks = 10;
            if (mc.thePlayer.hurtResistantTime <= 10 && damageLag <= 0 && mc.thePlayer.onGround) {
                PlayerUtils.damage();
                damageCount++;
                damageLag = 7;
                double vel = 0.24813599859094576;
                verticalVelocity += vel;
                verticalFreedom += verticalVelocity;
                verticalVelocityCounter = Math.min(100, Math.max(verticalVelocityCounter, VelocityData.GRACE_TICKS) + 1 + (int)Math.round(vel * 10.0D));
                verticalVelocityUsed = 0;
            }

            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                startY = mc.thePlayer.posY;
                PlayerUtils.debug("Damage Count: "+damageCount);
                jump(event);
                jumpListening = false;
            } else {
                PlayerUtils.skipTicks();
                event.setCancelPackets(true);
            }
        } else {
            if (verticalFreedom > prevFreedom) {
                mc.thePlayer.jumpTicks = 10;
                jump(event);
            } else {
                mc.thePlayer.motionY = 0;

            }
        }
        damageLag--;
        super.onPre(event);
    }

    private void jump(EventPreUpdate event) {
        mc.thePlayer.setPosition(mc.thePlayer.posX, startY + 1.35 + verticalFreedom, mc.thePlayer.posZ);
        event.setY(mc.thePlayer.posY);
        mc.thePlayer.motionY = 0;
        prevFreedom = verticalFreedom;
        tickVertical();
    }

    private void tickVertical() {
        if (this.verticalVelocity <= 0.09D) {
            this.verticalVelocityUsed++;
            this.verticalVelocityCounter--;
        } else if (this.verticalVelocityCounter > 0) {
            this.verticalVelocityUsed++;
            this.verticalFreedom += this.verticalVelocity;
            this.verticalVelocity = Math.max(0.0D, this.verticalVelocity - 0.09D);
        } else if (this.verticalFreedom > 0.001D) {
            if (this.verticalVelocityUsed == 1 && this.verticalVelocity > 1.0D) {
                this.verticalVelocityUsed = 0;
                this.verticalVelocity = 0.0D;
                this.verticalFreedom = 0.0D;
            } else {
                this.verticalVelocityUsed++;
                this.verticalFreedom *= 0.93D;
            }
        }
    }
}
