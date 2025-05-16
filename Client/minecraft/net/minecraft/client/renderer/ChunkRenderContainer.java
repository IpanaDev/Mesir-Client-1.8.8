package net.minecraft.client.renderer;

import baritone.Baritone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import org.lwjgl.opengl.GL14;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public abstract class ChunkRenderContainer
{
    protected double viewEntityX;
    protected double viewEntityY;
    protected double viewEntityZ;
    protected boolean initialized;

    public ChunkRenderContainer() {

    }

    public void initialize(double viewEntityXIn, double viewEntityYIn, double viewEntityZIn) {
        this.initialized = true;
        //this.renderChunks.clear();
        this.viewEntityX = viewEntityXIn;
        this.viewEntityY = viewEntityYIn;
        this.viewEntityZ = viewEntityZIn;
    }

    public void preRenderChunk(RenderChunk renderChunkIn) {
        BlockPos blockpos = renderChunkIn.getPosition();
        if (Baritone.settings().renderCachedChunks.value && !Minecraft.getMinecraft().isSingleplayer() && Minecraft.getMinecraft().theWorld.getChunkFromBlockCoords(renderChunkIn.getPosition()).isEmpty()) {
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GL14.glBlendColor(0, 0, 0, Baritone.settings().cachedChunksOpacity.value);
            GlStateManager.tryBlendFuncSeparate(GL_CONSTANT_ALPHA, GL_ONE_MINUS_CONSTANT_ALPHA, GL_ONE, GL_ZERO);
        }
        GlStateManager.translate((float)((double)blockpos.getX() - this.viewEntityX), (float)((double)blockpos.getY() - this.viewEntityY), (float)((double)blockpos.getZ() - this.viewEntityZ));
    }

    public abstract void renderChunkLayer(EnumWorldBlockLayer layer, List<RenderChunk> renderList);

    public boolean canRender(RenderChunk renderChunk, EnumWorldBlockLayer layer) {
        return renderChunk != null && renderChunk.getCompiledChunk().isLayerUsed(layer);
    }
}
