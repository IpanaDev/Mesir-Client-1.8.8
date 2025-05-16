package net.minecraft.client.renderer.culling;

import java.nio.FloatBuffer;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;

public class ClippingHelperImpl extends ClippingHelper {
    private static ClippingHelperImpl instance = new ClippingHelperImpl();
    private FloatBuffer projectionMatrixBuffer = GLAllocation.createDirectFloatBuffer(16);
    private FloatBuffer modelviewMatrixBuffer = GLAllocation.createDirectFloatBuffer(16);
    private FloatBuffer field_78564_h = GLAllocation.createDirectFloatBuffer(16);

    /**
     * Initialises the ClippingHelper object then returns an instance of it.
     */
    public static ClippingHelper getInstance() {
        instance.init();
        return instance;
    }

    private void normalize(float[] floats) {
        float f = MathHelper.sqrt_float(floats[0] * floats[0] + floats[1] * floats[1] + floats[2] * floats[2]);
        floats[0] /= f;
        floats[1] /= f;
        floats[2] /= f;
        floats[3] /= f;
    }

    public void init() {
        this.projectionMatrixBuffer.clear();
        this.modelviewMatrixBuffer.clear();
        this.field_78564_h.clear();
        GlStateManager.getFloat(2983, this.projectionMatrixBuffer);
        GlStateManager.getFloat(2982, this.modelviewMatrixBuffer);
        this.projectionMatrixBuffer.flip().limit(16);
        this.projectionMatrixBuffer.get(projectionMatrix);
        this.modelviewMatrixBuffer.flip().limit(16);
        this.modelviewMatrixBuffer.get(modelviewMatrix);
        this.clippingMatrix[0] = modelviewMatrix[0] * projectionMatrix[0] + modelviewMatrix[1] * projectionMatrix[4] + modelviewMatrix[2] * projectionMatrix[8] + modelviewMatrix[3] * projectionMatrix[12];
        this.clippingMatrix[1] = modelviewMatrix[0] * projectionMatrix[1] + modelviewMatrix[1] * projectionMatrix[5] + modelviewMatrix[2] * projectionMatrix[9] + modelviewMatrix[3] * projectionMatrix[13];
        this.clippingMatrix[2] = modelviewMatrix[0] * projectionMatrix[2] + modelviewMatrix[1] * projectionMatrix[6] + modelviewMatrix[2] * projectionMatrix[10] + modelviewMatrix[3] * projectionMatrix[14];
        this.clippingMatrix[3] = modelviewMatrix[0] * projectionMatrix[3] + modelviewMatrix[1] * projectionMatrix[7] + modelviewMatrix[2] * projectionMatrix[11] + modelviewMatrix[3] * projectionMatrix[15];
        this.clippingMatrix[4] = modelviewMatrix[4] * projectionMatrix[0] + modelviewMatrix[5] * projectionMatrix[4] + modelviewMatrix[6] * projectionMatrix[8] + modelviewMatrix[7] * projectionMatrix[12];
        this.clippingMatrix[5] = modelviewMatrix[4] * projectionMatrix[1] + modelviewMatrix[5] * projectionMatrix[5] + modelviewMatrix[6] * projectionMatrix[9] + modelviewMatrix[7] * projectionMatrix[13];
        this.clippingMatrix[6] = modelviewMatrix[4] * projectionMatrix[2] + modelviewMatrix[5] * projectionMatrix[6] + modelviewMatrix[6] * projectionMatrix[10] + modelviewMatrix[7] * projectionMatrix[14];
        this.clippingMatrix[7] = modelviewMatrix[4] * projectionMatrix[3] + modelviewMatrix[5] * projectionMatrix[7] + modelviewMatrix[6] * projectionMatrix[11] + modelviewMatrix[7] * projectionMatrix[15];
        this.clippingMatrix[8] = modelviewMatrix[8] * projectionMatrix[0] + modelviewMatrix[9] * projectionMatrix[4] + modelviewMatrix[10] * projectionMatrix[8] + modelviewMatrix[11] * projectionMatrix[12];
        this.clippingMatrix[9] = modelviewMatrix[8] * projectionMatrix[1] + modelviewMatrix[9] * projectionMatrix[5] + modelviewMatrix[10] * projectionMatrix[9] + modelviewMatrix[11] * projectionMatrix[13];
        this.clippingMatrix[10] = modelviewMatrix[8] * projectionMatrix[2] + modelviewMatrix[9] * projectionMatrix[6] + modelviewMatrix[10] * projectionMatrix[10] + modelviewMatrix[11] * projectionMatrix[14];
        this.clippingMatrix[11] = modelviewMatrix[8] * projectionMatrix[3] + modelviewMatrix[9] * projectionMatrix[7] + modelviewMatrix[10] * projectionMatrix[11] + modelviewMatrix[11] * projectionMatrix[15];
        this.clippingMatrix[12] = modelviewMatrix[12] * projectionMatrix[0] + modelviewMatrix[13] * projectionMatrix[4] + modelviewMatrix[14] * projectionMatrix[8] + modelviewMatrix[15] * projectionMatrix[12];
        this.clippingMatrix[13] = modelviewMatrix[12] * projectionMatrix[1] + modelviewMatrix[13] * projectionMatrix[5] + modelviewMatrix[14] * projectionMatrix[9] + modelviewMatrix[15] * projectionMatrix[13];
        this.clippingMatrix[14] = modelviewMatrix[12] * projectionMatrix[2] + modelviewMatrix[13] * projectionMatrix[6] + modelviewMatrix[14] * projectionMatrix[10] + modelviewMatrix[15] * projectionMatrix[14];
        this.clippingMatrix[15] = modelviewMatrix[12] * projectionMatrix[3] + modelviewMatrix[13] * projectionMatrix[7] + modelviewMatrix[14] * projectionMatrix[11] + modelviewMatrix[15] * projectionMatrix[15];
        float[] afloat2 = this.frustum[0];
        afloat2[0] = this.clippingMatrix[3] - this.clippingMatrix[0];
        afloat2[1] = this.clippingMatrix[7] - this.clippingMatrix[4];
        afloat2[2] = this.clippingMatrix[11] - this.clippingMatrix[8];
        afloat2[3] = this.clippingMatrix[15] - this.clippingMatrix[12];
        this.normalize(afloat2);
        float[] afloat3 = this.frustum[1];
        afloat3[0] = this.clippingMatrix[3] + this.clippingMatrix[0];
        afloat3[1] = this.clippingMatrix[7] + this.clippingMatrix[4];
        afloat3[2] = this.clippingMatrix[11] + this.clippingMatrix[8];
        afloat3[3] = this.clippingMatrix[15] + this.clippingMatrix[12];
        this.normalize(afloat3);
        float[] afloat4 = this.frustum[2];
        afloat4[0] = this.clippingMatrix[3] + this.clippingMatrix[1];
        afloat4[1] = this.clippingMatrix[7] + this.clippingMatrix[5];
        afloat4[2] = this.clippingMatrix[11] + this.clippingMatrix[9];
        afloat4[3] = this.clippingMatrix[15] + this.clippingMatrix[13];
        this.normalize(afloat4);
        float[] afloat5 = this.frustum[3];
        afloat5[0] = this.clippingMatrix[3] - this.clippingMatrix[1];
        afloat5[1] = this.clippingMatrix[7] - this.clippingMatrix[5];
        afloat5[2] = this.clippingMatrix[11] - this.clippingMatrix[9];
        afloat5[3] = this.clippingMatrix[15] - this.clippingMatrix[13];
        this.normalize(afloat5);
        float[] afloat6 = this.frustum[4];
        afloat6[0] = this.clippingMatrix[3] - this.clippingMatrix[2];
        afloat6[1] = this.clippingMatrix[7] - this.clippingMatrix[6];
        afloat6[2] = this.clippingMatrix[11] - this.clippingMatrix[10];
        afloat6[3] = this.clippingMatrix[15] - this.clippingMatrix[14];
        this.normalize(afloat6);
        float[] afloat7 = this.frustum[5];
        afloat7[0] = this.clippingMatrix[3] + this.clippingMatrix[2];
        afloat7[1] = this.clippingMatrix[7] + this.clippingMatrix[6];
        afloat7[2] = this.clippingMatrix[11] + this.clippingMatrix[10];
        afloat7[3] = this.clippingMatrix[15] + this.clippingMatrix[14];
        this.normalize(afloat7);
    }
}
