package ipana.modules.movement.modes.waterwalk;

import ipana.events.*;
import ipana.modules.movement.WaterWalk;
import ipana.utils.ncp.utilities.ActionAccumulator;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.BlockLiquid;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;

public class Dolphin extends WaterMode {
    private int ticks, cooldown;
    private boolean onWater;
    private int ekim29;
    private double lastDistY;
    private double lastDist;
    private ActionAccumulator vDistAcc = new ActionAccumulator(3, 3);

    public Dolphin(WaterWalk parent) {
        super("Dolphin", parent);
    }

    @Override
    public void onMove(EventMoving event) {
        double moveSpeed = 0;
        if (!onWater && !mc.thePlayer.isCollidedVertically) {
            if (mc.thePlayer.isInWater()) {
                ekim29 = -2173;
            }
            if (ekim29 >= 0) {
                double[] motions = new double[]{0.5, 0.4843, 0.4687};
                if (ekim29 < motions.length) {
                    mc.thePlayer.motionY = motions[ekim29];
                } else {
                    mc.thePlayer.motionY = decideBest(event.getY());
                }
                if (ekim29 > 0) {
                    mc.thePlayer.motionY = accumulate(mc.thePlayer.motionY);
                } else {
                    vDistAcc.clear();
                }
                mc.thePlayer.motionY -= 1E-7;
                event.setY(mc.thePlayer.motionY);
                ekim29++;
            }
        }
        if (ekim29 >= 0) {
            moveSpeed = PlayerUtils.getBaseMoveSpeed();
        }
        double[] water = new double[]{0.0626, 0.18};
        if (mc.thePlayer.isInWater() || mc.theWorld.getBlockState(mc.thePlayer.getPosition2()).getBlock() instanceof BlockLiquid) {
            if (cooldown < 0) {
                ticks = 0;
                cooldown++;
            }
            moveSpeed = water[ticks];
            if (mc.gameSettings.keyBindJump.pressed) {
                event.setY(mc.thePlayer.motionY = 0.13);
            } else if (mc.gameSettings.keyBindSneak.pressed) {
                event.setY(mc.thePlayer.motionY = -0.22);
            }
        }

        if (moveSpeed != 0) {
            double[] c = PlayerUtils.calculate(moveSpeed);
            event.setX(c[0]);
            event.setZ(c[1]);
        }
        ticks++;
        if (ticks >= 2) {
            ticks = 0;
        }
        super.onMove(event);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        if (mc.thePlayer.onGround) {
            ekim29 = -2173;
        }
        if (!mc.thePlayer.isInWater() && mc.theWorld.getBlockState(mc.thePlayer.getPosition2()).getBlock() instanceof BlockLiquid) {
            ekim29 = 0;
            onWater = false;
        }
        if (mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY-1E-4, mc.thePlayer.posZ)).getBlock() instanceof BlockLiquid && !(mc.theWorld.getBlockState(mc.thePlayer.getPosition2()).getBlock() instanceof BlockLiquid)) {
            //mc.thePlayer.expandPos(0,-0.08,0);
            //event.setY(event.getY()-0.08);
            //mc.thePlayer.motionY = -0.072173;
            ekim29 = -2173;
        }
        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
        lastDistY = mc.thePlayer.posY-mc.thePlayer.prevPosY;
        super.onPre(event);
    }

    @Override
    public void onSend(EventPacketSend event) {

        super.onSend(event);
    }

    @Override
    public void onReceive(EventPacketReceive event) {
        if (mc.thePlayer != null && mc.thePlayer.isInWater() && event.getState() == EventPacketReceive.PacketState.PRE && event.getPacket() instanceof S08PacketPlayerPosLook) {
            cooldown = -1;
        }
        super.onReceive(event);
    }

    @Override
    public void onBB(EventBoundingBox event) {
        if (mc.thePlayer != null && !mc.thePlayer.isInWater() && event.getBlock() instanceof BlockLiquid && mc.theWorld.getBlockState(event.getBlockPos()).getBlock() instanceof BlockLiquid && mc.theWorld.getBlockState(event.getBlockPos()).getValue(BlockLiquid.LEVEL) == 0 && !mc.thePlayer.isSneaking()) {
            double d = event.getBlockPos().getY() + 1;
            if (d <= mc.thePlayer.boundingBox.minY) {
                event.setBoundingBox(new AxisAlignedBB(event.getBlockPos().getX(), event.getBlockPos().getY(), event.getBlockPos().getZ(), event.getBlockPos().getX() + 1, event.getBlockPos().getY() + 0.93, event.getBlockPos().getZ() + 1));
            }
        }
        super.onBB(event);
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1;
        super.onDisable();
    }

    private double decideBest(double defaultDist) {
        boolean atat1 = lastDistY > -0.25D && lastDistY < 0.5D /*&& yDistance < lastDistY - 0.0312D && yDistance > lastDistY - 0.0834D*/;
        boolean atat2 = lastDistY < 0.1044D && lastDistY > 0.05D /*&& yDistance > 0.020000000000000004D && yDistance - lastDistY < -0.025D*/;
        boolean atat3 = lastDistY < 0.2D && lastDistY >= 0.0D /*&& yDistance > -0.2D && yDistance < 0.1668D*/;
        boolean atat4 = lastDistY > 0.020000000000000004D && lastDistY < 0.0624D /*&& yDistance == 0.0D*/;
        boolean atat5 = lastDistY < -0.204D && lastDistY-0.0125 > -0.26/*yDistance > -0.26D && yDistance-lastDistY > -0.0624D && yDistance-lastDistY < -0.0125D*/;
        boolean atat6 = lastDistY < -0.05D && lastDistY > -0.0624D /*&& yDistance > -0.29159999999999997D && yDistance < -0.0834D*/;
        boolean atat7 = lastDistY < 0.5D && lastDistY > 0.4D /*&& yDistance-lastDistY > -0.0624D && yDistance-lastDistY < -0.05D*/;
        boolean atat8 = lastDistY == 0.0D /*&& yDistance > -0.0624D && yDistance < -0.05D*/;

        ArrayList<Double> values = new ArrayList<>();
        if (atat1) {
            values.add(lastDistY-0.0312);
        }
        if (atat2) {
            values.add(lastDistY-0.025);
        }
        if (atat3) {
            values.add(0.1668);
        }
        if (atat4) {
            values.add(0.0);
        }
        if (atat5) {
            values.add(lastDistY-0.0125);
        }
        if (atat6) {
            values.add(-0.0834);
        }
        if (atat7) {
            values.add(lastDistY-0.05);
        }
        if (atat8) {
            values.add(-0.05);
        }
        if (values.isEmpty()) {
            return defaultDist;
        }
        return getMax(values);
    }

    private double getMax(ArrayList<Double> values) {
        double maxValue = values.get(0);
        for (double value : values) {
            maxValue = Math.max(maxValue, value);
        }
        return maxValue;
    }

    private double accumulate(double yDistance) {
        boolean yDirChange = lastDistY != yDistance && (yDistance <= 0.0D && lastDistY >= 0.0D || yDistance >= 0.0D && lastDistY <= 0.0D);
        boolean verVelUsed = true;
        if (yDirChange && lastDistY > 0) {
            vDistAcc.clear();
            vDistAcc.add((float) yDistance);
        } else if (verVelUsed) {
            if (yDistance != 0.0D) {
                vDistAcc.add((float) yDistance);
                double ataturkDistance = yDistance;
                double hacim = verticalAccounting(ataturkDistance, vDistAcc);
                while (hacim > 0) {
                    ataturkDistance -= 0.0001;
                    vDistAcc.changeBucket(0, vDistAcc.bucketScore(0)-0.0001f);
                    hacim = verticalAccounting(ataturkDistance, vDistAcc);
                }
                return ataturkDistance;
            }
        } else {
            vDistAcc.clear();
        }
        return yDistance;
    }

    private double verticalAccounting(double yDistance, ActionAccumulator acc) {
        int count0 = acc.bucketCount(0);
        if (count0 > 0) {
            int count1 = acc.bucketCount(1);
            if (count1 > 0) {
                int cap = acc.bucketCapacity();
                float sc0;
                if (count0 == cap) {
                    sc0 = acc.bucketScore(0);
                } else {
                    sc0 = acc.bucketScore(0) * (float) cap / (float) count0 - 0.03744F * (float) (cap - count0);
                }

                float sc1 = acc.bucketScore(1);
                if ((double) sc0 > (double) sc1 - 0.11231999471783638D) {
                    if (yDistance <= -1.05D && (double) sc1 < -8.0D && (double) sc0 < -8.0D) {
                        return 0.0D;
                    }
                    return (double) sc0 - ((double) sc1 - 0.11231999471783638D);
                }
            }
        }

        return 0.0D;
    }
}
