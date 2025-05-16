package net.minecraft.client.renderer;

import java.nio.ByteBuffer;
import java.util.List;

import ipana.managements.module.Modules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import optifine.Config;

import org.lwjgl.opengl.GL11;
import shadersmod.client.SVertexBuilder;

public class WorldVertexBufferUploader {
    private int vboId = -1;


    public void buildDrawArrays(WorldRenderer worldRenderer) {
        buildDrawArrays(worldRenderer, true);
    }

    public void buildDrawArrays(WorldRenderer worldRenderer, boolean autoState) {
        if (worldRenderer.getVertexCount() > 0) {
            VertexFormat vertexformat = worldRenderer.getVertexFormat();
            int i = vertexformat.getNextOffset();
            ByteBuffer bytebuffer = worldRenderer.getByteBuffer();
            List<VertexFormatElement> list = vertexformat.getElements();
            for (int j = 0; j < list.size(); ++j) {
                VertexFormatElement vertexformatelement = list.get(j);
                int l = vertexformatelement.getType().getGlConstant();
                int k = vertexformatelement.getIndex();
                bytebuffer.position(vertexformat.getOffset(j));
                switch (vertexformatelement.getUsage()) {
                    case POSITION -> {
                        GL11.glVertexPointer(vertexformatelement.getElementCount(), l, i, bytebuffer);
                        if (autoState) {
                            GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
                        }
                    }
                    case UV -> {
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + k);
                        GL11.glTexCoordPointer(vertexformatelement.getElementCount(), l, i, bytebuffer);
                        if (autoState) {
                            GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                        }
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                    }
                    case COLOR -> {
                        GL11.glColorPointer(vertexformatelement.getElementCount(), l, i, bytebuffer);
                        if (autoState) {
                            GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
                        }
                    }
                    case NORMAL -> {
                        GL11.glNormalPointer(l, i, bytebuffer);
                        if (autoState) {
                            GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
                        }
                    }
                }
            }

            if (worldRenderer.isMultiTexture()) {
                worldRenderer.drawMultiTexture();
            } else if (Config.isShaders()) {
                SVertexBuilder.drawArrays(worldRenderer.getDrawMode(), 0, worldRenderer.getVertexCount(), worldRenderer);
            } else {
                GL11.glDrawArrays(worldRenderer.getDrawMode(), 0, worldRenderer.getVertexCount());
            }


            for (VertexFormatElement element : list) {
                switch (element.getUsage()) {
                    case POSITION -> {
                        if (autoState) {
                            GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
                        }
                    }
                    case UV -> {
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit + element.getIndex());
                        if (autoState) {
                            GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
                        }
                        OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
                    }
                    case COLOR -> {
                        if (autoState) {
                            GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
                        }
                        GlStateManager.resetColor();
                    }
                    case NORMAL -> {
                        if (autoState) {
                            GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
                        }
                    }
                }
            }
        }

        worldRenderer.reset();
    }
}
