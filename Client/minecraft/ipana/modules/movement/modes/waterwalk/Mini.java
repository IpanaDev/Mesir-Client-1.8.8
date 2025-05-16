package ipana.modules.movement.modes.waterwalk;

import ipana.events.EventMoving;
import ipana.events.EventPacketReceive;
import ipana.modules.movement.WaterWalk;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.BlockLiquid;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;

public class Mini extends WaterMode {
    private int cooldown;

    public Mini(WaterWalk parent) {
        super("Mini", parent);
    }

    @Override
    public void onMove(EventMoving event) {
        double moveSpeed = 0;
        if (inLiquid() || mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY-0.07, mc.thePlayer.posZ)).getBlock() instanceof BlockLiquid) {
            moveSpeed = PlayerUtils.baseSpeed311(event.getX(), event.getZ());
            if (cooldown < 0) {
                moveSpeed = 0.0626;
                cooldown++;
            }
        }
        if (inLiquid()) {
            if (mc.gameSettings.keyBindJump.pressed) {
                event.setY(mc.thePlayer.motionY = 0.13);
            } else if (mc.gameSettings.keyBindSneak.pressed) {
                event.setY(mc.thePlayer.motionY = -0.22);
            } else if (!mc.thePlayer.isInWater() && !mc.thePlayer.isInLava()) {
                event.setY(mc.thePlayer.motionY = 0.08);
            }
        }
        if (moveSpeed != 0) {
            double[] c = PlayerUtils.calculate(moveSpeed);
            event.setX(c[0]);
            event.setZ(c[1]);
        }
        super.onMove(event);
    }

    @Override
    public void onReceive(EventPacketReceive event) {
        if (mc.thePlayer != null && inLiquid() && event.getState() == EventPacketReceive.PacketState.PRE && event.getPacket() instanceof S08PacketPlayerPosLook) {
            cooldown = -1;
        }
        super.onReceive(event);
    }

    private boolean inLiquid() {
        return mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || mc.theWorld.getBlockState(mc.thePlayer.getPosition2()).getBlock() instanceof BlockLiquid;
    }

}
