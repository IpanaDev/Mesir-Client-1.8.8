package net.minecraft.client.renderer;

import baritone.Baritone;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import optifine.Config;

import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_ZERO;

public class RenderList extends ChunkRenderContainer {
    public int trolollol = -1;

    public void renderChunkLayer(EnumWorldBlockLayer layer, List<RenderChunk> renderChunks) {
        if (this.initialized) {
            if (!Config.isVBORegions()) {
                for (RenderChunk renderchunk1 : renderChunks) {
                    if (canRender(renderchunk1, layer)) {
                        ListedRenderChunk listedrenderchunk1 = (ListedRenderChunk) renderchunk1;
                        GlStateManager.pushMatrix();
                        this.preRenderChunk(renderchunk1);
                        GL11.glCallList(listedrenderchunk1.getDisplayList(layer, listedrenderchunk1.getCompiledChunk()));
                        if (Baritone.settings().renderCachedChunks.value && !Minecraft.getMinecraft().isSingleplayer()) {
                            // reset the blend func to normal (not dependent on constant alpha)
                            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
                        }
                        GlStateManager.popMatrix();
                    }
                }
            } else {
                GlStateManager.pushMatrix();

                BlockPos blockpos = renderChunks.get(0).getPosition();
                GlStateManager.translate(blockpos.getX()-viewEntityX, blockpos.getY()-viewEntityY, blockpos.getZ()-viewEntityZ);
                GL11.glCallList(trolollol);
                if (Baritone.settings().renderCachedChunks.value && !Minecraft.getMinecraft().isSingleplayer()) {
                    // reset the blend func to normal (not dependent on constant alpha)
                    GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
                }
                GlStateManager.popMatrix();
            }

            if (Config.isMultiTexture()) {
                GlStateManager.bindCurrentTexture();
            }

            GlStateManager.resetColor();
        }
    }
}
