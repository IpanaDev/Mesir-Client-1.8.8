package ipana.utils.vbo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class DynamicVBO {
    private VertexBuffer buffer;
    private VertexFormat format;
    private int mode;
    private boolean containsPos, containsTex, containsCol, buildMode;
    private WorldRenderer worldRenderer;
    private boolean render3D;
    private boolean lightmap;

    public DynamicVBO(boolean render3D, boolean buildMode, boolean lightmap) {
        this.buildMode = buildMode;
        this.worldRenderer = Tessellator.getInstance().getWorldRenderer();
        this.render3D = render3D;
        this.lightmap = lightmap;
    }

    public DynamicVBO pos(double x, double y, double z) {
        worldRenderer.pos(x, y, z);
        return this;
    }
    public DynamicVBO tex(double u, double v) {
        worldRenderer.tex(u, v);
        return this;
    }

    public DynamicVBO color(Color color) {
        worldRenderer.color(color);
        return this;
    }

    public void end() {
        worldRenderer.endVertex();
    }
    public void preCompile(int mode, VertexFormat beginBuffer) {
        this.mode = mode;
        containsPos = false;
        containsTex = false;
        containsCol = false;
        if (format != null) {
            format.clear();
        }
        if (buffer != null) {
            buffer.deleteGlBuffers();
        }
        format = new VertexFormat();
        for (VertexFormatElement element : beginBuffer.getElements()) {
            if (element == DefaultVertexFormats.POSITION_3F) {
                containsPos = true;
            } else if (element == DefaultVertexFormats.TEX_2F) {
                containsTex = true;
            } else if (element == DefaultVertexFormats.COLOR_4UB) {
                containsCol = true;
            }
            format.func_181721_a(element);
        }

        buffer = new VertexBuffer(format);
        if (buildMode) {
            worldRenderer.begin(mode, beginBuffer);
        }
    }

    public void postCompile() {
        if (buildMode) {
            worldRenderer.finishDrawing();
        }
        worldRenderer.reset();
        loadToBuffer();
    }

    public void loadToBuffer() {
        buffer.uploadBufferData(worldRenderer.getByteBuffer());
    }

    public void draw() {
        enableStates();
        buffer.bindBuffer();
        if (render3D) {
            pointer3d();
        } else {
            pointer2d();
        }
        buffer.drawArrays(mode);
        buffer.unbindBuffer();
        disableStates();
    }

    private void pointer3d() {
        int stride = 28;
        if (containsPos)
            GL11.glVertexPointer(3, GL11.GL_FLOAT, stride, 0);

        if (containsCol)
            GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, stride, 12);

        if (containsTex) {
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glTexCoordPointer(2, GL11.GL_FLOAT, stride, 16L);
            if (lightmap) {
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
                GL11.glTexCoordPointer(2, GL11.GL_SHORT, 28, 24);
                OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            }
        }
    }

    private void pointer2d() {
        int stride = 24;
        if (containsPos)
            GL11.glVertexPointer(3, GL11.GL_FLOAT, stride, 0);

        if (containsTex)
            GL11.glTexCoordPointer(2, GL11.GL_FLOAT, stride, 12);

        if (containsCol)
            GL11.glColorPointer(4, GL11.GL_UNSIGNED_BYTE, stride, 20);
    }

    private void enableStates() {
        if (lightmap) {
            GlStateManager.enableColorLogic();
            Minecraft.getMinecraft().entityRenderer.enableLightmap();
            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        } else {
            if (containsPos) {
                GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
            }
            if (containsTex) {
                GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
            }
            if (containsCol)
                GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
        }
    }

    private void disableStates() {
        if (lightmap) {
            for (VertexFormatElement vertexformatelement : format.getElements()) {
                VertexFormatElement.EnumUsage vertexformatelement$enumusage = vertexformatelement.getUsage();
                int i = vertexformatelement.getIndex();

                switch (vertexformatelement$enumusage) {
                    case POSITION -> GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                    case UV -> {
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + i);
                        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                    }
                    case COLOR -> {
                        GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
                        GlStateManager.resetColor();
                    }
                }
            }
            Minecraft.getMinecraft().entityRenderer.disableLightmap();
            GlStateManager.disableColorLogic();
        } else {
            if (containsPos)
                GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

            if (containsTex)
                GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

            if (containsCol)
                GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
        }
    }

    public WorldRenderer worldRenderer() {
        return worldRenderer;
    }
}
