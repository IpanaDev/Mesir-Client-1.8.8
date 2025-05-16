package ipana.utils.gl;

import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;

import java.nio.FloatBuffer;
import java.util.function.Consumer;

public class GLMatrix {
    private FloatBuffer buffer;
    //Matrix Views are GL_MODELVIEW_MATRIX (2982), GL_PROJECTION_MATRIX (2983), GL_TEXTURE_MATRIX (2984)
    public GLMatrix(int MATRIX_VIEW, Consumer<Boolean> consumer) {
        buffer = GLAllocation.createDirectFloatBuffer(16);
        setupMatrix(MATRIX_VIEW, consumer);
    }

    public void setupMatrix(int MATRIX_VIEW, Consumer<Boolean> consumer) {
        //GL.pushMatrix & GL.popMatrix not included in consumer
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        consumer.accept(true);
        GlStateManager.getFloat(MATRIX_VIEW, buffer);
        GlStateManager.popMatrix();
    }

    public void multMatrix() {
        GlStateManager.multMatrix(buffer);
    }
}
