package ipana.modules.combat.modes.target;

import ipana.events.EventMoving;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Modules;
import ipana.modules.combat.KillAura;
import ipana.modules.combat.Target;
import ipana.modules.movement.Speed;
import ipana.modules.movement.modes.speed.NCPOnGround;
import ipana.modules.player.AutoDrink;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

public class DefaultTarget extends TargetMode {
    private EntityLivingBase blinkTarget;
    private int blinkCount;
    private double startBlinkX;
    private double startBlinkY;
    private double startBlinkZ;

    public DefaultTarget(Target parent) {
        super("Default", parent);
    }

    @Override
    public void onMove(EventMoving event) {
        Target parent = getParent();
        KillAura ka = Modules.KILL_AURA;
        Speed speedM = Modules.SPEED;
        AutoDrink autoDrink = Modules.AUTO_DRINK;

        if (blinkTarget != null && (!ka.isEnabled() || blinkCount > 200)) {
            resetBlink();
        }

        EntityLivingBase target = blinkTarget != null ? blinkTarget : ka.curTar;
        if (target == null)
            return;

        double m = parent.speed.getValue();
        double range = ka.range.getValue();
        double d0 = (target.posX + MathHelper.clamp_double((target.posX - target.prevPosX)*m, -range, range));
        double d1 = (target.posY + MathHelper.clamp_double((target.posY - target.prevPosY)*m, -range, range));
        double d2 = (target.posZ + MathHelper.clamp_double((target.posZ - target.prevPosZ)*m, -range, range));
        double dist = mc.thePlayer.getDistance(d0,d1,d2);
        float yaw = RotationUtils.getRotationFromPosition(d0,d2,d1)[0];
        double speed = Math.sqrt(event.getX() * event.getX() + event.getZ() * event.getZ());
        float strafeYaw = parent.angle.getValue() / (float)dist;

        if (parent.blink.getValue()) {
            decideBlink(target, ka, speedM);
        } else {
            resetBlink();
        }


        boolean strategyCheck = autoDrink.isEnabled() && autoDrink.moveStrategy.runTicks > 0;
        if (dist <= 0.7 && !strategyCheck) {
            boolean speedCheck = speedM.isEnabled() && PlayerUtils.isMoving2();
            boolean onground = speedCheck && (speedM.mode.getValue() instanceof NCPOnGround);
            speed = onground ? speed : 0.09;
            strafeYaw = 180;
        }
        if (blinkTarget != null) {
            speed = 0;
        }
        double[] c = PlayerUtils.calculate(speed, yaw, strafeYaw);
        event.setX(c[0]);
        event.setZ(c[1]);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        if (blinkTarget != null) {
            event.setCancelPackets(true);
            blinkCount++;
        }
    }

    private void decideBlink(EntityLivingBase targetBase, KillAura ka, Speed speedM) {
        if (!(targetBase instanceof EntityOtherPlayerMP target)) {
            return;
        }
        long diff = System.currentTimeMillis() - target.lastPosUpdateMS;
        long diffInterval = 160;//update can be down by 1 tick and 1 tick can be down by 10 ms
        //TODO: update aura's target so it won't switch to other targets than blinkTarget

        if (diff >= diffInterval) {
            if (blinkTarget == null && (target.getAverageSpeed(10) > 0.45)) {
                PlayerUtils.debug(ka.curTar.getName() + " started blinking.");
                startBlinkX = target.otherPlayerMPX;
                startBlinkY = target.otherPlayerMPY;
                startBlinkZ = target.otherPlayerMPZ;
                blinkTarget = ka.curTar;
            }
        } else {
            if (blinkTarget != null) {
                double x = target.otherPlayerMPX;
                double y = target.otherPlayerMPY;
                double z = target.otherPlayerMPZ;
                double xDiff = x - startBlinkX;
                double yDiff = y - startBlinkY;
                double zDiff = z - startBlinkZ;
                double blinkDist = Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
                if (blinkDist <= 0.1) {
                    return;
                }
                PlayerUtils.debug(blinkTarget.getName() + " (" + blinkCount + ") stopped blinking.");
                float yaw = RotationUtils.getRotationFromPosition(x, z, y)[0];
                double lastDist = Double.MAX_VALUE;
                for (int i = 0; i < blinkCount; i++) {
                    double nextSpeed = speedM.nextSpeed();
                    double nextY = speedM.nextY();

                    double[] c = PlayerUtils.calculate2(nextSpeed, yaw, 1f);
                    mc.thePlayer.expandPos(c[0], 0, c[1]);
                    PlayerUtils.sendOffset(0, nextY, 0);

                    double dist = mc.thePlayer.getDistance(x, y, z);
                    if (dist > lastDist) {
                        break;
                    }
                    lastDist = dist;
                }
                blinkTarget = null;
                blinkCount = 0;
            }
        }
    }

    private void resetBlink() {
        blinkTarget = null;
        blinkCount = 0;
    }

    public boolean check() {
        Target target = getParent();
        KillAura ka = Modules.KILL_AURA;
        return (!ka.targets.isEmpty() || blinkTarget != null) && mc.thePlayer.moveForward > 0 && (!target.sword.getValue() || ka.canBlock());
    }
}
