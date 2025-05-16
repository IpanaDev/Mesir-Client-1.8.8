package ipana.modules.movement.modes.fly;

import ipana.events.EventMoveInput;
import ipana.events.EventMoving;
import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Modules;
import ipana.modules.combat.KillAura;
import ipana.modules.movement.Fly;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.BlockLiquid;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.List;

import static ipana.utils.player.PlayerUtils.calculate;
import static ipana.utils.player.PlayerUtils.getBaseMoveSpeed;

public class OldNCPGlide extends FlyMode {
    public OldNCPGlide(Fly parent) {
        super("OldNCPGlide", parent);
    }
    private static final double MAX_GLIDE = -0.03125;
    private Fly fly = getParent();
    public int flyLimit;
    public boolean suicideOperation;
    public boolean offsetUP;
    private int kaTicks;
    private double lastYDist;
    private double posY;
    private int movingTicks;
    private int up;
    private double lastDist;
    private int ticks;

    @Override
    public void onEnable() {
        suicideOperation = false;
        if (mc.thePlayer != null) {
            mc.thePlayer.stepHeight = 0.6f;
            posY = mc.thePlayer.boundingBox.minY;
            if (mc.thePlayer.isCollidedVertically) {
                PlayerUtils.damage();
                movingTicks = 1;
                mc.thePlayer.motionY = 0.42;
            }
        }
        up = 1;
        resetTick();
        resetLimit();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.thePlayer.stepHeight = 0.6f;
        super.onDisable();
    }

    @Override
    public void onMove(EventMoving event) {
        double hAllowedDistance = getBaseMoveSpeed(0.29, true);
        double someThreshold = hAllowedDistance / 3.3;
        double speed = 0;
        if (movingTicks == 1) {
            speed = hAllowedDistance * 2 + someThreshold;
        } else if (movingTicks == 2) {
            speed = hAllowedDistance + someThreshold;
        } else if (movingTicks >= 3) {
            speed = lastDist - lastDist / 130 - 1E-5;
            if (mc.thePlayer.prevOnIce && !mc.thePlayer.onIce) {
                speed = hAllowedDistance+someThreshold;
            }
        }
        speed = Math.max(speed, hAllowedDistance);
        if (event.getY() <= 0 && mc.gameSettings.keyBindSneak.pressed) {
            speed = 0;
        }
        double[] calc = calculate(speed);
        event.setX(calc[0]);
        event.setZ(calc[1]);
        super.onMove(event);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        Fly.GlideMode glideMode = fly.glideMode.getValue();
        boolean isCrit = glideMode == Fly.GlideMode.Crit;
        boolean isNonCrit = glideMode == Fly.GlideMode.NonCrit;
        boolean isManual = glideMode == Fly.GlideMode.Manual;
        if (posY>=mc.thePlayer.boundingBox.minY && !mc.thePlayer.isCollidedVertically) {
            if (up == 1) {
                mc.thePlayer.motionY = posY-mc.thePlayer.boundingBox.minY-1.0E-4;
            } else {
                if (isNonCrit || isManual) {
                    event.setOnGround(Modules.KILL_AURA.targets.isEmpty());
                }
                if (mc.gameSettings.keyBindJump.pressed) {
                    mc.thePlayer.motionY = Math.min(fly.speed.getValue(), 9.9);
                    if (ticks <= 30) {
                        resetTick();
                        if (flyLimit <= 0) {
                            ticks = 0;
                        }
                    }
                }
                if (mc.gameSettings.keyBindSneak.pressed) {
                    mc.thePlayer.motionY = Math.max(-fly.speed.getValue(), -9.9);
                    resetTick();
                }
                double glideMotion = MAX_GLIDE - 1E-5;
                if ((!mc.gameSettings.keyBindSneak.pressed && !mc.gameSettings.keyBindJump.pressed) || flyLimit <= 1) {
                    KillAura ka = Modules.KILL_AURA;
                    boolean hasTarget = ka.isEnabled() && !ka.targets.isEmpty() && ka.curTar != null;
                    mc.thePlayer.motionY = (isCrit || isNonCrit) ? glideMotion : hasTarget ? -0.07 : glideMotion;
                    if (hasTarget) {
                        kaTicks++;
                    }
                }
                ticks--;
                if (ticks <= 0) {
                    if (fly.autoDisable.getValue() && flyLimit <= 0) {
                        fly.toggle();
                        return;
                    }
                    if (flyLimit > 0) {
                        int critAdjust = Math.min(kaTicks, 29);
                        mc.thePlayer.motionY = -glideMotion * ((isCrit || isNonCrit) ? 29 : (29-critAdjust) + 0.07*critAdjust);
                        event.setOnGround(true);
                        resetTick();
                    } else {
                        PlayerUtils.debug("Gliding down to get collision");
                        offsetUP = true;
                        ticks = 100000;
                    }
                }
                if (offsetUP && flyLimit > 0) {
                    mc.thePlayer.motionY = 9.9;
                }
                if (mc.thePlayer.motionY < 0) {
                    AxisAlignedBB ground = null;
                    double groundOffset = 0.61;//additional 0.01 kills all ncp flags nice
                    for (int i = 0; i > (int)(mc.thePlayer.motionY-groundOffset); i--) {
                        List<AxisAlignedBB> bbList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0, i, 0));
                        if (!bbList.isEmpty()) {
                            ground = getMaxY(bbList);
                            break;
                        }
                    }
                    if (ground == null) {
                        List<AxisAlignedBB> bbList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0, mc.thePlayer.motionY-groundOffset, 0));
                        if (!bbList.isEmpty()) {
                            ground = getMaxY(bbList);
                        }
                    }
                    if (ground != null) {
                        mc.thePlayer.motionY = Math.max(ground.maxY - mc.thePlayer.boundingBox.minY + groundOffset, mc.thePlayer.motionY);
                    }
                }
                if (mc.thePlayer.boundingBox.minY + mc.thePlayer.motionY >= posY) {
                    mc.thePlayer.motionY = posY - mc.thePlayer.boundingBox.minY - 1.0E-4;
                    offsetUP = false;
                    resetTick();
                }
            }
            up = 0;
        }
        if (isCrit) {
            //event.setOnGround(!suicideOperation);
            if (suicideOperation && (PlayerUtils.isInLiquid() || mc.theWorld.getBlockState(mc.thePlayer.getPosition2()).getBlock() instanceof BlockLiquid)) {
                mc.thePlayer.motionY = -1;
                PlayerUtils.debug("Suicide operation başarılı!");
                fly.toggle();
            }
        }
        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
        lastYDist = mc.thePlayer.posY - mc.thePlayer.prevPosY;
        movingTicks++;
        super.onPre(event);
    }

    private AxisAlignedBB getMaxY(List<AxisAlignedBB> bbList) {
        AxisAlignedBB bb = null;
        int y = Integer.MIN_VALUE;
        for (AxisAlignedBB alignedBB : bbList) {
            if (y < alignedBB.maxY) {
                bb = alignedBB;
            }
        }
        return bb;
    }

    @Override
    public void onPost(EventPostUpdate event) {
        AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().expand(0.0625, 0.0625, 0.0625).addCoord(0.0D, -0.55D, 0.0D);
        if (mc.thePlayer.posY - mc.thePlayer.prevPosY >= MAX_GLIDE) {
            flyLimit--;
            Modules.HUD.markArraylistDirty();
        }
        if (mc.theWorld.checkBlockCollision(bb)) {
            resetLimit();
        }
        super.onPost(event);
    }

    @Override
    public Object getSuffix() {
        return flyLimit;
    }

    public void resetLimit() {
        flyLimit = 80;
    }

    private void resetTick() {
        ticks = 30;
        kaTicks = 0;
    }

    @Override
    public void onMove(EventMoveInput event) {
        super.onMove(event);
    }
}
