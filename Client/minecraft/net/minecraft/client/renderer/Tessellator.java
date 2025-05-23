package net.minecraft.client.renderer;

public class Tessellator
{
    private WorldRenderer worldRenderer;
    public WorldVertexBufferUploader vboUploader = new WorldVertexBufferUploader();

    /** The static instance of the Tessellator. */
    private static final Tessellator instance = new Tessellator(2097152);

    public static Tessellator getInstance()
    {
        return instance;
    }

    public Tessellator(int bufferSize)
    {
        this.worldRenderer = new WorldRenderer(bufferSize);
    }
    /**
     * Draws the data set up in this tessellator and resets the state to prepare for new drawing.
     */
    public void draw() {
        this.draw(true);
    }
    public void draw(boolean autoState) {
        this.worldRenderer.finishDrawing();
        this.vboUploader.buildDrawArrays(this.worldRenderer, autoState);
    }
    public WorldRenderer getWorldRenderer()
    {
        return this.worldRenderer;
    }
}
