package net.minecraft.client.renderer.vertex;

import java.nio.ByteBuffer;

import ipana.utils.vbo.VboRange;
import ipana.utils.vbo.VboRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.util.EnumWorldBlockLayer;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

public class VertexBuffer {
    private int glBufferId;
    private final VertexFormat vertexFormat;
    private int count;
    private VboRegion vboRegion;
    private VboRange vboRange;

    public VertexBuffer(VertexFormat vertexFormatIn) {
        this.vertexFormat = vertexFormatIn;
        this.glBufferId = OpenGlHelper.glGenBuffers();
    }

    public void bindBuffer() {
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, this.glBufferId);
    }

    public void uploadBufferData(ByteBuffer p_181722_1_) {
        if (this.vboRegion != null) {
            this.vboRegion.bufferData(p_181722_1_, this.vboRange);
        } else {
            this.bindBuffer();
            OpenGlHelper.glBufferData(OpenGlHelper.GL_ARRAY_BUFFER, p_181722_1_, OpenGlHelper.GL_STATIC_DRAW);
            this.unbindBuffer();
            this.count = p_181722_1_.limit() / this.vertexFormat.getNextOffset();
        }
    }

    public void drawArrays(int mode) {
        if (this.vboRegion != null) {
            this.vboRegion.drawArrays(this.vboRange);
        } else {
            GL11.glDrawArrays(mode, 0, this.count);
        }
    }

    public void unbindBuffer() {
        OpenGlHelper.glBindBuffer(OpenGlHelper.GL_ARRAY_BUFFER, 0);
    }

    public void deleteGlBuffers() {
        if (this.glBufferId >= 0) {
            OpenGlHelper.glDeleteBuffers(this.glBufferId);
            this.glBufferId = -1;
        }
    }

    public void setVboRegion(VboRegion p_setVboRegion_1_) {
        if (p_setVboRegion_1_ != null) {
            this.deleteGlBuffers();
            this.vboRegion = p_setVboRegion_1_;
            this.vboRange = new VboRange();
        }
    }

    public VboRegion getVboRegion() {
        return this.vboRegion;
    }
}
