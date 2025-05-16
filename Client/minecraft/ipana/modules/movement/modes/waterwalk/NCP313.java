package ipana.modules.movement.modes.waterwalk;

import ipana.events.EventBoundingBox;
import ipana.events.EventMoving;
import ipana.events.EventPacketReceive;
import ipana.events.EventPacketSend;
import ipana.modules.movement.WaterWalk;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.BlockLiquid;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;

public class NCP313 extends WaterMode{
    private int ticks, cooldown;
    private boolean ataturk;
    private boolean onWater;

    public NCP313(WaterWalk parent) {
        super("NCP 3.13", parent);
    }

    @Override
    public void onMove(EventMoving event) {
        double moveSpeed = 0;
        double[] water = new double[]{0.0626, 0.18};//TODO: faster possible
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
    public void onSend(EventPacketSend event) {
        if (event.getState() == EventPacketSend.PacketState.PRE && event.getPacket() instanceof C03PacketPlayer c03) {
            if (mc.thePlayer != null && !mc.thePlayer.isInWater() && mc.theWorld.getBlockState(mc.thePlayer.getPosition2().add(0,-1E-4,0)).getBlock() instanceof BlockLiquid && onWater) {
                if (ataturk) {
                    c03.y -= 1E-6;
                }
                ataturk = !ataturk;
                onWater = false;
            } else {
                ataturk = false;
            }
        }
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
                event.setBoundingBox(new AxisAlignedBB(event.getBlockPos().getX(), event.getBlockPos().getY(), event.getBlockPos().getZ(), event.getBlockPos().getX() + 1, event.getBlockPos().getY() + 1, event.getBlockPos().getZ() + 1));
                onWater = true;
            }
        }
        super.onBB(event);
    }

    @Override
    public void onDisable() {
        mc.timer.timerSpeed = 1;
        super.onDisable();
    }
}
