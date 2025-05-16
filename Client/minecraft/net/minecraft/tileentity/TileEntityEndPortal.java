package net.minecraft.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class TileEntityEndPortal extends TileEntity
{


    public TileEntityEndPortal() {
        setRenderer(TileEntityRendererDispatcher.instance.tileEntityEndPortalRenderer);
    }
}
