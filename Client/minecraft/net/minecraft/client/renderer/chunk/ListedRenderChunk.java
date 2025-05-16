package net.minecraft.client.renderer.chunk;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;

public class ListedRenderChunk extends RenderChunk {
    private final int baseDisplayList = GLAllocation.generateDisplayLists(EnumWorldBlockLayer.VALUES.length);
    private int chunkIndex;
    private boolean willBeRendered;

    public ListedRenderChunk(World worldIn, RenderGlobal renderGlobalIn, BlockPos pos) {
        super(worldIn, renderGlobalIn, pos);
    }

    public int getDisplayList(EnumWorldBlockLayer layer, CompiledChunk p_178600_2_)
    {
        return p_178600_2_.isLayerUsed(layer) ? this.baseDisplayList + layer.ordinal() : -1;
    }

    public int chunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int _chunkIndex) {
        chunkIndex = _chunkIndex;
    }

    public void deleteGlResources()
    {
        super.deleteGlResources();
        GLAllocation.deleteDisplayLists(this.baseDisplayList, EnumWorldBlockLayer.VALUES.length);
    }

    public boolean willBeRendered() {
        return willBeRendered;
    }

    public void setWillBeRendered(boolean _willBeRendered) {
        willBeRendered = _willBeRendered;
    }
}
