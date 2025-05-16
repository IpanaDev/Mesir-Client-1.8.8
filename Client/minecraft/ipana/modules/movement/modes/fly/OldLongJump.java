package ipana.modules.movement.modes.fly;

import ipana.events.EventMoveInput;
import ipana.events.EventMoving;
import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.modules.movement.Fly;
import ipana.utils.StringUtil;
import ipana.utils.math.MathUtils;
import ipana.utils.ncp.utilities.ActionAccumulator;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.util.List;

import static ipana.utils.player.PlayerUtils.*;

public class OldLongJump extends FlyMode{
    public OldLongJump(Fly parent) {
        super("OldLongJump", parent);
    }

    private double x,z;
    private Method method;
    private double lastGlideY;
    private int hBufCount;
    private int ticks;
    private int movingTicks;
    private boolean naber;
    private double lastDist;

    @Override
    public void onEnable() {
        Fly fly = getParent();
        ticks = -1;
        movingTicks = 0;
        naber = false;
        super.onEnable();
    }

    @Override
    public void onMove(EventMoving event) {
        Fly fly = getParent();
        if (naber && mc.thePlayer.onGround) {
            if (fly.autoDisable.getValue()) {
                double[] speed = PlayerUtils.calculate(0.09);
                event.setX(speed[0]);
                event.setZ(speed[1]);
                //Print H distance
                //PlayerUtils.debug(mc.thePlayer.getDistance(x, mc.thePlayer.posY, z));
                fly.toggle();
                return;
            } else {
                ticks = -1;
                naber = false;
                movingTicks = 0;
                hBufCount = 0;
            }
        }
        if (mc.thePlayer.motionY < 0) {
            //Glide
            if (!mc.thePlayer.isCollidedVertically) {
                double startY = isLong() ? 0.078 : 0.115;
                double[] jumpBoostsLong = {0.051, 0.031};
                double[] jumpBoostsShort = {0.0811, 0.0394};
                double[] jumpBoosts = isLong() ? jumpBoostsLong : jumpBoostsShort;
                glide(event, startY, jumpBoosts);
            }
        } else {
            //Boosting Y because jump Y is always 0.42
            if (movingTicks == 1) {
                boolean jumpPot = mc.thePlayer.isPotionActive(Potion.jump);
                int amp = jumpPot ? mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() : -1;
                double boostY = isLong() ? 0.077 : 0.02;
                if (jumpPot) {
                    double[] boostsLong = {0.166, 0.348, 0.2999, 0.406};
                    double[] boostsShort = {0.135, 0.288, 0.2999, 0.406};
                    double[] boosts = isLong() ? boostsLong : boostsShort;
                    int effect = Math.min(amp, boosts.length - 1);
                    boostY = boosts[effect];
                }
                event.setY(mc.thePlayer.motionY = event.getY() + boostY);
            }
        }
        double hAllowedDistance = getBaseMoveSpeed(0.29, true);
        double someThreshold = hAllowedDistance / 3.3;
        double spd = hAllowedDistance;
        double bunnySlope = (hAllowedDistance+someThreshold);
        double[] calculatedHBuf = calcHBuf(hAllowedDistance, bunnySlope);
        double hBufSpeed = calculatedHBuf[0];
        //H Buffer
        if (ticks < 0) {
            spd = 0.07;
            if (ncpListener().sfHorizontalBuffer >= 1) {
                ticks = 0;
            }
        }
        //H Speed
        if (movingTicks > 0) {
            if (movingTicks == 1) {
                spd = bunnySlope;
                hBufCount = 0;
            }
            if (movingTicks > 1) {
                if (hBufCount < calculatedHBuf[1]) {
                    if (movingTicks % 2 == 0) {
                        spd = hBufSpeed;
                        hBufCount++;
                    } else {
                        spd = bunnySlope;
                    }
                } else {
                    hBufCount = 2173;
                    spd = lastDist - lastDist / 130d - 1E-7;
                    if (spd > bunnySlope) {
                        spd = hAllowedDistance;
                    }
                    spd = Math.max(spd, hAllowedDistance);
                }
            }
            movingTicks++;
        }
        mc.timer.timerSpeed = 1;
        if (PlayerUtils.isMoving2() && ticks == 0) {
            double[] d = calculateLong();
            List<AxisAlignedBB> headCollision = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0, d[0], 0));
            method = headCollision.isEmpty() ? Method.LONG : Method.SHORT;
            if (isLong()) {
                sendOffset(0,d[0],0,true);
                sendOffset(0,d[1],0,true);
                sendOffset(0,d[2],0,true);
                //mc.thePlayer.iceTicks-=3;
                //Timer Speed can be decreased here
            }
            event.setY(mc.thePlayer.motionY=0.42);
            spd = (hAllowedDistance*2+someThreshold);
            x = mc.thePlayer.posX;
            z = mc.thePlayer.posZ;
            hBufCount = 0;
            ticks = 1;
            naber = true;
            movingTicks = 1;
        }
        List<AxisAlignedBB> collideCurrentTick = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0D, mc.thePlayer.motionY, 0.0D));
        List<AxisAlignedBB> collideNextTick = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0.0D, mc.thePlayer.motionY-0.0625, 0.0D));
        if (!collideNextTick.isEmpty() && collideCurrentTick.isEmpty()) {
            event.setY(mc.thePlayer.motionY -= 0.0625);
        }
        double[] speed = PlayerUtils.calculate(spd);
        event.setX(speed[0]);
        event.setZ(speed[1]);
        super.onMove(event);
    }

    @Override
    public void onMove(EventMoveInput event) {
        super.onMove(event);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        Fly fly = getParent();
        if (!fly.isEnabled()) {
            return;
        }
        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
        super.onPre(event);
    }

    @Override
    public void onPost(EventPostUpdate event) {
        super.onPost(event);
    }

    private double[] calcHBuf(double hAllowedDistance, double bunnySlope) {
        int i = 4;
        double ref = 1d/i+hAllowedDistance;
        while (ref < bunnySlope) {
            i--;
            if (i <= 0) {
                break;
            }
            ref = 1d/i+hAllowedDistance;
        }
        return new double[]{ref, i};
    }

    private void glide(EventMoving event, double startY, double[] jumpBoosts) {
        Fly fly = getParent();
        boolean jumpPot = mc.thePlayer.isPotionActive(Potion.jump);
        int amp = jumpPot ? mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() : -1;
        double whatThisBroAyooooooooo = 1.1E-4 / 3d;
        double lastGlideDist = 0.0208 + whatThisBroAyooooooooo;
        double glideStart = startY;
        if (jumpPot) {
            glideStart = jumpBoosts[Math.min(amp, jumpBoosts.length-1)];
        }
        if (ticks > 0) {
            if (ticks == 1) {
                lastGlideY = -glideStart;
            }
            mc.thePlayer.motionY = lastGlideY;
            if (ticks % 3 == 0) {
                lastGlideY -= lastGlideDist;
            }
        }
        ticks++;
        event.setY(mc.thePlayer.motionY);
    }

    private double[] calculateLong() {
        double[] d = new double[10];
        double gn = 1E-7;
        double startY = 1.35-gn;
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            startY += 0.6 + mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier();
        }
        double nextY = startY;
        double bypass = 0.6-gn;
        for (int i = 0; i < d.length; i++) {
            int tick = i % 3;
            if (tick == 0) {
                d[i] = nextY;
            } else if (tick == 1) {
                d[i] = nextY - (nextY - bypass) / 2 - gn;
            } else {
                d[i] = d[i - 1] - (nextY - bypass) / 2 + gn;
                nextY = startY + nextY - (nextY - bypass) / 2 - gn;
            }
        }
        return d;
    }

    private boolean isLong() {
        return method == Method.LONG;
    }
    enum Method {
        SHORT, LONG
    }
}