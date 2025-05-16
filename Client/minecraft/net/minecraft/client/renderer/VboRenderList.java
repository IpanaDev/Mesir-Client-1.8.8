package net.minecraft.client.renderer;

import baritone.Baritone;
import ipana.managements.module.Modules;
import ipana.utils.vbo.VboRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.EnumWorldBlockLayer;
import optifine.Config;
import org.lwjgl.opengl.GL11;
import shadersmod.client.ShadersRender;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class VboRenderList extends ChunkRenderContainer {

    public void renderChunkLayer(EnumWorldBlockLayer layer, List<RenderChunk> chunks) {
        if (this.initialized) {
            if (!Config.isVBORegions()) {
                for (RenderChunk renderchunk1 : chunks) {
                    if (canRender(renderchunk1, layer)) {
                        VertexBuffer vertexbuffer1 = renderchunk1.getVertexBufferByLayer(layer.ordinal());
                        GlStateManager.pushMatrix();
                        this.preRenderChunk(renderchunk1);
                        renderchunk1.multModelviewMatrix();
                        vertexbuffer1.bindBuffer();
                        this.setupArrayPointers();
                        vertexbuffer1.drawArrays(7);
                        if (Baritone.settings().renderCachedChunks.value && !Minecraft.getMinecraft().isSingleplayer()) {
                            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
                        }
                        GlStateManager.popMatrix();
                    }
                }
                OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
            } else {
                VboRegion vboregion = null;

                for (RenderChunk renderchunk : chunks) {
                    VertexBuffer vertexbuffer = renderchunk.getVertexBufferByLayer(layer.ordinal());
                    VboRegion vboRegion = vertexbuffer.getVboRegion();
                    if (vboRegion != null) {
                        vertexbuffer.drawArrays(7);
                        vboregion = vboRegion;
                    }
                }
                if (vboregion != null) {
                    this.drawRegion(vboregion);
                }
            }
            GlStateManager.resetColor();
        }
    }

    public void setupArrayPointers() {
        if (Config.isShaders()) {
            ShadersRender.setupArrayPointersVbo();
        } else {
            boolean funny = Modules.BRIGHTNESS.isEnabled() && Modules.BRIGHTNESS.funny.getValue();
            GL11.glVertexPointer(3, GL11.GL_FLOAT, 28, 0);
            GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, 28, funny ? 13L : 12L);
            GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 28, 16L);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glTexCoordPointer(2, GL11.GL_SHORT, 28, 24L);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
        }
    }

    private void drawRegion(VboRegion vboRegion) {
        RenderGlobal global = Minecraft.getMinecraft().renderGlobal;
        GlStateManager.pushMatrix();
        GlStateManager.translate(global.viewerRegionX - this.viewEntityX, -this.viewEntityY,global.viewerRegionZ - this.viewEntityZ);
        vboRegion.finishDraw(this);
        if (Baritone.settings().renderCachedChunks.value && !Minecraft.getMinecraft().isSingleplayer()) {
            // reset the blend func to normal (not dependent on constant alpha)
            GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO);
        }
        GlStateManager.popMatrix();
    }
}
