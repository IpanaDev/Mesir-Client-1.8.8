package ipana.modules.movement.modes.speed;


import ipana.events.EventMoving;
import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.events.EventSetBack;
import ipana.modules.movement.Speed;
import ipana.utils.math.Pair;
import ipana.utils.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.List;

import static ipana.utils.player.PlayerUtils.calculate;
import static ipana.utils.player.PlayerUtils.getBaseMoveSpeed;

public class NCPOnGround extends SpeedMode {
    public NCPOnGround(Speed parent) {
        super("NCPOnGround", parent);
    }

    private boolean shouldJump;
    private boolean flagged;
    private boolean slopped;
    private double moveY;
    private List<Pair<Integer, Double>> pairs = new ArrayList<>();

    @Override
    public void onEnable() {
        flagged = false;
        super.onEnable();
    }

    @Override
    public void onMoving(EventMoving event) {
        Speed speed = getParent();
        double hAllowedBase = getBaseMoveSpeed();
        boolean sfDirty = PlayerUtils.ncpListener().data().horizontalVelocity().hasAny();
        if (PlayerUtils.isMoving2()) {
            if (flagged || speed.lastDist < hAllowedBase - 1E-4) {
                double bok = flagged ? 0 : PlayerUtils.ncpListener().sfHorizontalBuffer;
                speed.spd = hAllowedBase + bok;
                speed.ticks = 1;
                slopped = false;
                flagged = false;
            } else {
                switch (speed.ticks) {
                    case 0 -> {
                        speed.spd = hAllowedBase + PlayerUtils.ncpListener().sfHorizontalBuffer;
                        slopped = false;
                        speed.ticks = 1;
                    }
                    case 1 -> {
                        if (mc.thePlayer.onGround) {
                            speed.spd =
                                    speed.ncp316.getValue() && !sfDirty ?
                                            hAllowedBase * 1.65 :
                                            speed.lastDist * 2.15 - 1E-7;

                            if (mc.gameSettings.keyBindJump.pressed) {
                                event.setY(mc.thePlayer.motionY = offsetY());
                                shouldJump = false;
                            } else {
                                shouldJump = true;
                            }
                            mc.thePlayer.jumpTicks = 10;
                            slopped = false;
                            speed.ticks = 2;
                        } else if (slopped) {
                            speed.spd = speed.lastDist - speed.lastDist / 160 - 1E-7;
                        } else {
                            speed.spd = hAllowedBase;
                        }
                    }
                    case 2 -> {
                        double difference = 0.66 * (speed.lastDist - hAllowedBase);
                        speed.spd =
                                speed.ncp316.getValue() ?
                                        hAllowedBase :
                                        speed.lastDist - difference;


                        double aboveLimit = 4.0;
                        double hFreedom = PlayerUtils.testHFreedom(aboveLimit);
                        double freedomSpeed = hAllowedBase + hFreedom;
                        //PlayerUtils.debug((average - speed.spd));
                        if (freedomSpeed - speed.spd > 0.1 && !speed.ncp316.getValue()) {
                            hFreedom = PlayerUtils.getHFreedomNoCopy(aboveLimit);
                            //PlayerUtils.debug("hFreedom: " + hFreedom);
                            //PlayerUtils.debug("Diff: "+(freedomSpeed - speed.spd));
                            speed.spd = hAllowedBase + hFreedom;
                        }

                        slopped = true;
                        speed.ticks = 1;
                    }
                }
            }
        }
        moveY = mc.thePlayer.posY;
        speed.spd = Math.max(speed.spd, hAllowedBase);
        speed.spd = Math.min(speed.spd, 9.99);
        //PlayerUtils.debug(speed.ticks+", "+speed.spd);
        double[] d = calculate(speed.spd);
        event.setX(d[0]);
        event.setZ(d[1]);

        super.onMoving(event);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        Speed speed = getParent();

        speed.timerBoost();

        if (shouldJump) {
            double jumpY = offsetY();
            //Did we step on a slab? If so an extra jump is unnecessary
            if (mc.thePlayer.posY - moveY < jumpY) {
                //Head Obstruct Check
                double minY = minY(jumpY);
                double offset =
                        minY != Double.MAX_VALUE ?
                                minY - mc.thePlayer.boundingBox.maxY :
                                jumpY;

                event.setY(event.getY() + offset);
            } else if (mc.thePlayer.onGround) {
                //Well we have stepped + we are on ground, so we can boost twice (2x 2.15 boost wow!)
                speed.ticks = 1;
            }
            shouldJump = false;
        }

        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        double hSpeed = Math.hypot(xDist, zDist);

        //PlayerUtils.debug(speed.ticks+" : "+hSpeed/speed.lastDist+" : "+((speed.lastDist - hSpeed) / (speed.lastDist - PlayerUtils.getBaseMoveSpeed())));


        speed.lastDist = hSpeed;
        //BPS
        pairs.add(Pair.of(Minecraft.getRunTick(), speed.lastDist));
        pairs.removeIf(f -> Minecraft.getRunTick()-f.first() >= 20);
        double sum = 0;
        for (var pair : pairs) {
            sum += pair.second();
        }

        super.onPre(event);
    }

    @Override
    public void onPost(EventPostUpdate event) {
        super.onPost(event);
    }

    @Override
    public void onSetBack(EventSetBack event) {
        if (!event.cancelPacket()) {
            event.setResetMotion(false);
            flagged = true;
        }
        super.onSetBack(event);
    }

    private double minY(double y) {
        double minY = Double.MAX_VALUE;
        List<AxisAlignedBB> collisions = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(0, y, 0));
        for (AxisAlignedBB bb : collisions) {
            minY = Math.min(minY, bb.minY);
        }
        return minY;
    }

    private boolean isOnGround(double x, double z) {
        return !mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.boundingBox.offset(x, -1E-4, z)).isEmpty();
    }

    private double offsetY() {
        double val = 0.4;
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            val += (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
        }
        return val;
    }
}
