package ipana.utils.render;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import ipana.utils.player.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;

public class GLUProjection {
    private final static Minecraft mc = Minecraft.getMinecraft();
    
    public static float[] convert(float x, float y, float z, Entity ent) {
        float[] c = convertTo2D(x,y,z);
        if (!(c[2] < 0.0D) && !(c[2] >= 1.0D)) {
            float[] c2 = convertTo2D(x, y + 1.0f, z, ent);
            float[] c3 = convertTo2D(x, y + 1.0f, z, ent);
            return new float[]{c[0], c[1], Math.abs(c2[1] - c3[1]), c3[2]};
        }
        return new float[]{-1,-1,-1,-1};
    }
    public static float[] convert(float x, float y, float z) {
        return convertTo2D(x,y,z);
    }
    private static float[] convertTo2D(float x, float y, float z, Entity ent) {
        float pTicks = mc.timer.renderPartialTicks;
        float prevYaw = mc.thePlayer.rotationYaw;
        float prevPrevYaw = mc.thePlayer.prevRotationYaw;
        float[] rotations = RotationUtils.getRotationFromPosition(ent.lastTickPosX + (ent.posX - ent.lastTickPosX) * (double)pTicks, ent.lastTickPosZ + (ent.posZ - ent.lastTickPosZ) * (double)pTicks, ent.lastTickPosY + (ent.posY - ent.lastTickPosY) * (double)pTicks - 1.6D);
        mc.getRenderViewEntity().rotationYaw = mc.getRenderViewEntity().prevRotationYaw = rotations[0];
        mc.entityRenderer.setupCameraTransform(pTicks, 0);
        float[] convertedPoints = convertTo2D(x, y, z);
        mc.getRenderViewEntity().rotationYaw = prevYaw;
        mc.getRenderViewEntity().prevRotationYaw = prevPrevYaw;
        mc.entityRenderer.setupCameraTransform(pTicks, 0);
        return convertedPoints;
    }

    private static float[] convertTo2D(float x, float y, float z) {
        FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
        IntBuffer viewport = BufferUtils.createIntBuffer(16);
        FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
        FloatBuffer projection = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(2982, modelView);
        GL11.glGetFloat(2983, projection);
        GL11.glGetInteger(2978, viewport);
        boolean result = GLU.gluProject(x, y, z, modelView, projection, viewport, screenCoords);
        return result ? new float[]{screenCoords.get(0), Display.getHeight() - screenCoords.get(1), screenCoords.get(2)} : new float[]{-1,-1,-1,-1};
    }

}
