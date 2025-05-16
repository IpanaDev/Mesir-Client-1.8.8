package ipana.utils.chunk;

import ipana.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumFacing;

public class DynamicBlockRenderer {
    private Minecraft mc = Minecraft.getMinecraft();

    public boolean canBeSeen(EnumFacing side) {
        PlayerUtils.Face8Direction[] playerFace = mc.thePlayer.getFaces();
        return !((playerFace[0].isOpposite(side.getOpposite()) || playerFace[1].isOpposite(side.getOpposite())) && side != EnumFacing.UP && side != EnumFacing.DOWN);
    }

    public void checkAround(int range) {
        int atat = range;
        for (int x = -atat; x < atat; x++) {
            for (int y = -atat; y < atat; y++) {
                for (int z = -atat; z < atat; z++) {
                    Block block = mc.theWorld.getBlock(mc.thePlayer.getPosition2().getX()+x,mc.thePlayer.getPosition2().getY()+y,mc.thePlayer.getPosition2().getZ()+z);

                }
            }
        }
    }
}
