package ipana.modules.movement.modes.fly;

import ipana.events.*;
import ipana.modules.movement.Fly;
import ipana.utils.FutureTick;
import ipana.utils.math.MathUtils;
import ipana.utils.ncp.utilities.ActionAccumulator;
import ipana.utils.ncp.utilities.Magic;
import ipana.utils.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.List;

import static ipana.utils.player.PlayerUtils.*;

public class LongJump extends FlyMode {
    public LongJump(Fly parent) {
        super("LongJump", parent);
    }

    private double lastYDist;
    private double startX, startY, startZ;
    private double flagX, flagY, flagZ;
    private int slowTimer;
    private int lifeTime;
    private int ticks;
    private int jumpPhase;
    private double prevSpeed;
    private boolean waitForFlag;
    private boolean jumped;
    private boolean flagJumped;
    private ActionAccumulator vDistAcc = new ActionAccumulator(3, 3);

    @Override
    public void onEnable() {
        reset();
        resetHCombined();
        this.flagJumped = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onMove(EventMoveInput event) {
        super.onMove(event);
    }

    @Override
    public void onMove(EventMoving event) {
        Fly fly = getParent();
        if (waitForFlag) {
            event.setX(0);
            event.setZ(0);
            return;
        }
        if (!PlayerUtils.isMoving2()) {
            event.setX(mc.thePlayer.motionX = 0);
            event.setY(mc.thePlayer.motionY = 0);
            event.setZ(mc.thePlayer.motionZ = 0);
            return;
        }

        if (jumped && isOnGround()) {
            double horizontalDist = Math.hypot(mc.thePlayer.posX - startX, mc.thePlayer.posZ - startZ);
            //PlayerUtils.debug(horizontalDist);
            reset();
            resetHCombined();
            if (fly.autoDisable.getValue() && fly.isEnabled()) {
                fly.toggle();
                mc.timer.timerSpeed = 1f;
            }
        }

        if (ticks == 0 && ncpListener().sfHorizontalBuffer < 1) {
            double buff = 0.063;
            double[] test = calculate(buff);
            List<AxisAlignedBB> collidingList = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(test[0], -1E-4, test[1]));
            if (collidingList.isEmpty()) {
                buff = -buff;
            }
            event.setSpeed(buff);
            lifeTime--;
            return;
        }

        if (ticks > 1 && !isOnGround()) {
            glide(event, mc.thePlayer.motionY <= 0);
        }

        double baseSpeed = getBaseMoveSpeed();
        double moveSpeed = switch (ticks) {
            case 0 -> {
                ticks++;
                double horizontalBuff = baseSpeed + ncpListener().sfHorizontalBuffer;
                if (!fly.latest.getValue()) {
                    horizontalBuff += PlayerUtils.getHFreedomNoCopy(9.9 / 2.15 - horizontalBuff);
                }
                yield horizontalBuff;
            }
            case 1 -> {
                ticks++;
                startX = mc.thePlayer.posX;
                startY = mc.thePlayer.posY;
                startZ = mc.thePlayer.posZ;
                jumped = true;
                jumpPhase = 0;
                vDistAcc.clear();
                event.setY(mc.thePlayer.motionY = jumpHeight());
                yield Math.min(9.9, prevSpeed * 2.15 - 1E-7);
            }
            case 2 -> {
                ticks++;
                double difference = 0.66 * (prevSpeed - baseSpeed);
                yield prevSpeed - difference;
            }
            default -> {
                double troll = 0.020000000000000018;
                double troll2 = 33.3;
                double value1 = prevSpeed - prevSpeed / 160;
                double value2 = (prevSpeed + baseSpeed * troll) / (1 + troll);
                double value3 = (prevSpeed + baseSpeed / troll2) / (1 + 1 / troll2);
                yield Math.max(value1, Math.max(value2, value3)) - 1E-7;
            }
        };
        boolean stopped = fly.latest.getValue() && fly.stopLimit.getValue() && lifeTime-- <= 0;

        if (mc.thePlayer.motionY >= 0 && ++jumpPhase > maxJumpPhase()) {
            event.setY(mc.thePlayer.motionY = -1E-13);
        }

        if (ticks >= 3 && (!stopped || mc.gameSettings.keyBindSneak.pressed)) {
            event.setY(mc.thePlayer.motionY = inAirChecks(mc.thePlayer.motionY));
        }

        if (stopped) {
            moveSpeed = 0;
            if (!mc.gameSettings.keyBindSneak.pressed) {
                event.setY(mc.thePlayer.motionY = 0);
            }
        }

        event.setSpeed(moveSpeed);
        prepareLanding(event);
        super.onMove(event);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        Fly fly = getParent();
        double xDiff = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double yDiff = mc.thePlayer.posY - mc.thePlayer.prevPosY;
        double zDiff = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;

        if (xDiff == 0 && yDiff == 0 && zDiff == 0 && !mc.gameSettings.keyBindJump.pressed) {
            event.setCancelPackets(true);
            return;
        }

        if (PlayerUtils.isMoving2()) {
            prevSpeed = Math.hypot(xDiff, zDiff);
        } else {
            mc.thePlayer.motionY = 0;
            event.setCancelPackets(true);
        }

        lastYDist = yDiff;


        if (mc.gameSettings.keyBindJump.pressed) {
            mc.thePlayer.setPosition(startX, startY, startZ);
            event.setX(startX);
            event.setY(startY);
            event.setZ(startZ);
            event.setCancelPackets(false);
            PlayerUtils.cancelFlag(startX, startY, startZ, false, false);
            this.flagJumped = true;
        }

        super.onPre(event);
    }

    @Override
    public void onPost(EventPostUpdate event) {
        Fly fly = getParent();
        if (slowTimer-- == 0) {
            mc.timer.timerSpeed = 1;
        }
        super.onPost(event);
    }

    private void prepareLanding(EventMoving event) {
        List<AxisAlignedBB> nextColliding = mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0, mc.thePlayer.motionY-0.0625, 0));
        if (mc.thePlayer.motionY < 0 && !isOnGround() && !nextColliding.isEmpty()) {
            event.setY(mc.thePlayer.motionY -= 0.0625);
            ticks = 0;
        }
    }

    private void glide(EventMoving event, boolean falling) {
        if (!falling) {
            if (mc.thePlayer.isPotionActive(Potion.jump)) {
                mc.thePlayer.motionY = lastYDist * 0.98 - 0.0624 - 1E-13;
            }
        } else {
            mc.thePlayer.motionY = Magic.oddGravity(lastYDist, startY, mc.thePlayer.prevPosY);
            if (mc.thePlayer.motionY == Double.NEGATIVE_INFINITY) {
                mc.thePlayer.motionY = lastYDist * 0.98 - 0.0624 - 1E-13;
            }
        }
        double maxJump = 1.35;
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            maxJump += 0.6 + mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier();
        }
        double nextY = mc.thePlayer.posY + mc.thePlayer.motionY;
        double vTravel = nextY - startY;
        if (vTravel > maxJump) {
            mc.thePlayer.motionY -= vTravel - maxJump + 1E-13;
        }
        if (1E-13 > mc.thePlayer.motionY && -1E-13 < mc.thePlayer.motionY) {
            mc.thePlayer.motionY = 0;
        }
        event.setY(mc.thePlayer.motionY);
    }

    @Override
    public void onSetBack(EventSetBack event) {
        double predictX = MathUtils.fixFormat(flagX, 5);
        double predictY = MathUtils.fixFormat(flagY, 5);
        double predictZ = MathUtils.fixFormat(flagZ, 5);
        double flagX = MathUtils.fixFormat(event.xOff(), 5);
        double flagY = MathUtils.fixFormat(event.yOff(), 5);
        double flagZ = MathUtils.fixFormat(event.zOff(), 5);
        boolean wantedFlag = waitForFlag && flagX == predictX && flagY == predictY && flagZ == predictZ;
        if (event.state() == EventSetBack.State.PRE) {
            Fly fly = getParent();
            if (fly.autoDisable.getValue() && !wantedFlag && fly.isEnabled() && !event.cancelSetPos() && !event.cancelPacket()) {
                fly.toggle();
            }
        } else if (event.state() == EventSetBack.State.POST) {
            if (wantedFlag) {
                mc.thePlayer.setPosition(mc.thePlayer.posX, MathUtils.fixFormat(mc.thePlayer.posY - 0.0786, 5), mc.thePlayer.posZ);
                waitForFlag = false;
                reset();
            }
        }
        super.onSetBack(event);
    }

    @Override
    public void onReceive(EventPacketReceive event) {
        super.onReceive(event);
    }

    private void reset() {
        ticks = 0;
        lifeTime = 31;
        jumped = false;
        waitForFlag = false;
    }

    private void resetHCombined() {
        Fly fly = getParent();

        if (mc.thePlayer != null && fly.latest.getValue()) {
            double END_VALUE = 0.016;
            double INC = 0.0626;

            sendOffset(0,0,0);
            sendOffset(0, END_VALUE + INC * 2 + 1E-4, 0);
            sendOffset(0, END_VALUE + INC, 0);
            sendOffset(0, END_VALUE, 0);
            sendOffset(0, 0.45, 0);

            if (fly.instantFlag.getValue()) {
                cancelFlag(mc.thePlayer.posX, mc.thePlayer.posY + END_VALUE + INC, mc.thePlayer.posZ, false);
            } else {
                flagX = mc.thePlayer.posX;
                flagY = mc.thePlayer.posY + END_VALUE + INC;
                flagZ = mc.thePlayer.posZ;
                waitForFlag = true;
            }
            slowTimer = 5;
            mc.timer.timerSpeed = 0.75f;
        }
    }

    private double jumpHeight() {
        double val = 0.42;
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            val += (mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
        }
        return val;
    }

    private boolean isOnGround() {
        return !mc.theWorld.getCollidingBlockBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(0.0D, -1E-4, 0.0D)).isEmpty();
    }

    private double maxJumpPhase() {
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            return (int) Math.round((0.5D + mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 6);
        } else {
            return 6;
        }
    }

    private double inAirChecks(double yDist) {
        boolean yDirChange = lastYDist != yDist && (yDist <= 0.0D && lastYDist >= 0.0D || yDist >= 0.0D && lastYDist <= 0.0D);
        if (yDirChange && lastYDist > 0.0D) {
            vDistAcc.clear();
            vDistAcc.add((float) yDist);
        } else {
            if (yDist != 0.0D) {
                vDistAcc.add((float) yDist);
                double preCalc = verticalAccounting(yDist);
                vDistAcc.changeBucket(0, (float) (vDistAcc.bucketScore(0) - preCalc));
                yDist -= preCalc;
           }
        }
        return yDist;
    }

    private double verticalAccounting(double yDistance) {
        int count0 = vDistAcc.bucketCount(0);
        if (count0 > 0) {
            int count1 = vDistAcc.bucketCount(1);
            if (count1 > 0) {
                int cap = vDistAcc.bucketCapacity();
                float sc0;
                if (count0 == cap) {
                    sc0 = vDistAcc.bucketScore(0);
                } else {
                    sc0 = vDistAcc.bucketScore(0) * (float) cap / (float) count0 - 0.03744F * (float) (cap - count0);
                }
                float sc1 = vDistAcc.bucketScore(1);
                if ((double) sc0 > (double) sc1 - 0.11231999471783638D) {
                    if (yDistance <= -1.05D && (double) sc1 < -8.0D && (double) sc0 < -8.0D) {
                        return 0;
                    }

                    return vDistAcc.bucketScore(0) - (float)count0 * (0.03744F * (cap - count0) + sc1 - 0.11231999471783638D) / (float)cap + 1E-6;
                }
            }
        }

        return 0;
    }
}
