package net.minecraft.client.renderer;

import net.minecraft.client.renderer.vertex.VertexBuffer;

public class VertexBufferUploader extends WorldVertexBufferUploader {
    private VertexBuffer vertexBuffer = null;

    public void buildDrawArrays(WorldRenderer worldRenderer)
    {
        worldRenderer.reset();
        this.vertexBuffer.uploadBufferData(worldRenderer.getByteBuffer());
    }

    public void setVertexBuffer(VertexBuffer vertexBufferIn)
    {
        this.vertexBuffer = vertexBufferIn;
    }
}
