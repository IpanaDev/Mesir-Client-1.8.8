package ipana.events;

import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import pisi.unitedmeows.eventapi.event.Event;

public class EventBoundingBox extends Event {
    private Block block;
    private BlockPos blockPos;
    private AxisAlignedBB boundingBox;

    public EventBoundingBox(Block block, BlockPos pos, AxisAlignedBB boundingBox)
    {
        this.block = block;
        this.blockPos = pos;
        this.boundingBox = boundingBox;
    }

    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }
}
