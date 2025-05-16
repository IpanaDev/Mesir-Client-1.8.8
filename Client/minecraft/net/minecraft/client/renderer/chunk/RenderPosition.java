package net.minecraft.client.renderer.chunk;

import net.minecraft.util.BlockPos;

public class RenderPosition {
    private BlockPos pos;

    public RenderPosition(BlockPos pos) {
        this.pos = pos;
    }

    public BlockPos pos() {
        return pos;
    }

    public RenderPosition setPos(BlockPos _pos) {
        pos = _pos;
        return this;
    }
}
