package net.minecraft.client.renderer.chunk;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;

public class CompiledChunk
{
    public static final CompiledChunk DUMMY = new CompiledChunk()
    {
        protected void setLayerUsed(EnumWorldBlockLayer layer)
        {
            throw new UnsupportedOperationException();
        }
        public void setLayerStarted(EnumWorldBlockLayer layer)
        {
            throw new UnsupportedOperationException();
        }
    };
    //IF THERE IS A RENDER-ABLE (SIDE CHECKS ETC.) BLOCK IN A CHUNK
    private final boolean[] layersUsed = new boolean[EnumWorldBlockLayer.VALUES.length];
    //IF THERE IS A BLOCK IN A CHUNK
    private final boolean[] layersStarted = new boolean[EnumWorldBlockLayer.VALUES.length];
    //IF THERE IS A RENDER-ABLE (SIDE CHECKS ETC.) BLOCK IN A CHUNK
    public final boolean[][] visibilityCache = new boolean[6][6];
    private boolean empty = true;
    private WorldRenderer.State state;

    public boolean isEmpty()
    {
        return this.empty;
    }

    protected void setLayerUsed(EnumWorldBlockLayer layer)
    {
        this.empty = false;
        this.layersUsed[layer.ordinal()] = true;
    }

    public boolean isLayerUsed(EnumWorldBlockLayer layer)
    {
        return this.layersUsed[layer.ordinal()];
    }

    public void setLayerStarted(EnumWorldBlockLayer layer)
    {
        this.layersStarted[layer.ordinal()] = true;
    }

    public boolean isLayerStarted(EnumWorldBlockLayer layer)
    {
        return this.layersStarted[layer.ordinal()];
    }

    public boolean isVisible(EnumFacing facing, EnumFacing facing2)
    {
        return this.visibilityCache[facing.ordinal()][facing2.ordinal()];
    }

    public void setVisibility(SetVisibility visibility)
    {
        for (EnumFacing facing1 : EnumFacing.VALUES) {
            for (EnumFacing facing2 : EnumFacing.VALUES) {
                visibilityCache[facing1.ordinal()][facing2.ordinal()] = visibility.isVisible(facing1, facing2);
            }
        }
    }

    public WorldRenderer.State getState()
    {
        return this.state;
    }

    public void setState(WorldRenderer.State stateIn)
    {
        this.state = stateIn;
    }
}
