package ipana.modules.movement.modes.fly;

import ipana.events.*;
import ipana.modules.movement.Fly;
import ipana.utils.FutureTick;
import ipana.utils.player.PlayerUtils;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

import static ipana.utils.player.PlayerUtils.*;

public class OldNCPV2 extends FlyMode {
    public OldNCPV2(Fly parent) {
        super("OldNCPV2", parent);
    }
    private int ticks;
    private int lifeTime;
    private boolean gotHit;
    private double prevSpeed;
    private int lastCurrentItem;
    private int bowTicks;

    @Override
    public void onEnable() {
        lifeTime = 84;
        ticks = 0;
        gotHit = false;
        Fly fly = getParent();
        if (mc.thePlayer != null && fly.latest.getValue()) {
            double END_VALUE = 0.016;
            double INC = 0.0626;

            sendOffset(0,0,0);
            sendOffset(0, END_VALUE + INC * 2 + 1E-4, 0);
            sendOffset(0, END_VALUE + INC, 0);
            sendOffset(0, END_VALUE, 0);
            sendOffset(0, 0.45, 0);

            cancelFlag(mc.thePlayer.posX, mc.thePlayer.posY + END_VALUE + INC, mc.thePlayer.posZ, false);

            for (int i = 0; i < 9; i++) {
                var stack = mc.thePlayer.inventory.mainInventory[i];
                if (stack != null && stack.getItem() instanceof ItemBow) {
                    lastCurrentItem = mc.thePlayer.inventory.currentItem;
                    bowTicks = 0;
                    mc.thePlayer.inventory.currentItem = i;
                    mc.playerController.syncCurrentPlayItem();
                    PlayerUtils.packet(new C08PacketPlayerBlockPlacement(null));
                    break;
                }
            }
        }
        super.onEnable();
    }

    @Override
    public void onMove(EventMoving event) {
        Fly fly = getParent();
        if (ticks == 0 && fly.latest.getValue() && !gotHit) {
            if (mc.thePlayer.hurtResistantTime > 0) {
                gotHit = true;
            } else {
                if (bowTicks++ == 2) {
                    PlayerUtils.send(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, -90, true);
                    mc.thePlayer.lastReportedPitch = -90;
                    PlayerUtils.packet(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.UP));
                    mc.thePlayer.inventory.currentItem = lastCurrentItem;
                    mc.playerController.syncCurrentPlayItem();
                }
                event.setX(0);
                event.setZ(0);
                return;
            }
        }
        if (ticks == 0 && PlayerUtils.ncpListener().sfHorizontalBuffer < 1) {
            setSpeed(0.063);
            return;
        }

        if (ticks > 3 && event.getY() < 0) {
            int index = ticks - 4;
            double abiGotumeNcpKacti = 0.0312 + 1E-6;
            double[] glideY = new double[]{
                    0.049, -1E-13,
                    -abiGotumeNcpKacti, -abiGotumeNcpKacti * 2, -abiGotumeNcpKacti * 3,
                    -0.118604
            };
            event.setY(mc.thePlayer.motionY = (index < glideY.length ? glideY[index] : 0));
        }

        double baseSpeed = PlayerUtils.getBaseMoveSpeed();
        double moveSpeed = switch (ticks) {
            case 0 -> {
                ticks++;
                double horizontalBuff = PlayerUtils.getBaseMoveSpeed() + ncpListener().sfHorizontalBuffer;
                yield horizontalBuff + (fly.latest.getValue() ? 0 : PlayerUtils.getHFreedomNoCopy(9.9 / 2.15 - horizontalBuff));
            }
            case 1 -> {
                ticks++;
                event.setY(mc.thePlayer.motionY = 0.42);
                if (!fly.latest.getValue()) {
                    PlayerUtils.damage();
                }
                yield prevSpeed * 2.15 - 1E-6;
            }
            case 2 -> {
                ticks++;
                double difference = 0.66 * (prevSpeed - baseSpeed);
                yield prevSpeed - difference;
            }
            default -> {
                ticks++;
                double troll = 0.02;
                double troll2 = 33.3;
                double value1 = prevSpeed - prevSpeed / 160;
                double value2 = (prevSpeed + baseSpeed * troll) / (1 + troll);
                double value3 = (prevSpeed + baseSpeed / troll2) / (1 + 1 / troll2);

                yield Math.max(value1, Math.max(value2, value3)) - 1E-7;
            }
        };

        double spd = Math.max(moveSpeed, baseSpeed);
        double[] c = calculate(spd);
        event.setX(c[0]);
        event.setZ(c[1]);
        super.onMove(event);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        mc.timer.timerSpeed = 1f;
        double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double yDist = mc.thePlayer.posY - mc.thePlayer.prevPosY;
        double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
        if (xDist == 0 && yDist == 0 && zDist == 0 && ticks != 0) {
            event.setCancelPackets(true);
            return;
        }
        prevSpeed = Math.hypot(xDist, zDist);
        super.onPre(event);
    }

    @Override
    public void onPost(EventPostUpdate event) {
        Fly fly = getParent();
        if (ticks >= 1 && lifeTime-- <= 0 && fly.autoDisable.getValue()) {
            FutureTick.addFuture(1, fly::toggle);
        }
        super.onPost(event);
    }
}
