package ipana.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class EffectRenderUtils {
    private static ShaderGroup blurShader;
    private static Minecraft mc = Minecraft.getMinecraft();
    private static Framebuffer buffer;
    private static int lastScale;
    private static int lastScaleWidth;
    private static int lastScaleHeight;
    private static ResourceLocation shader = new ResourceLocation("shaders/post/blur.json");


    EffectRenderUtils(String effectName) {
        shader = new ResourceLocation("shaders/post/"+effectName+".json");
    }


    public static void initFboAndShader() {
        try {
            blurShader = new ShaderGroup(mc.getTextureManager(), mc.getResourceManager(), mc.getFramebuffer(), shader);
            blurShader.createBindFramebuffers(mc.displayWidth, mc.displayHeight);
            buffer = blurShader.getFbos().get(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setBlurConfigs(float intensity, float blurWidth, float blurHeight) {

        blurShader.getShaders().get(0).getShaderManager().getShaderUniform("Radius").set(intensity);
        blurShader.getShaders().get(1).getShaderManager().getShaderUniform("Radius").set(intensity);

        blurShader.getShaders().get(0).getShaderManager().getShaderUniform("BlurDir").set(blurWidth, blurHeight);
        blurShader.getShaders().get(1).getShaderManager().getShaderUniform("BlurDir").set(blurHeight, blurWidth);

    }
    public static void setPhosphorConfigs(float multiplier) {
        blurShader.getShaders().get(0).getShaderManager().getShaderUniform("Phosphor").set(multiplier,multiplier,multiplier);
    }
    public static void blurArea(float x, float y, float width, float height, float intensity, float blurWidth, float blurHeight) {
        ScaledResolution scale = RenderUtils.SCALED_RES;
        int factor = scale.getScaleFactor();
        int factor2 = scale.getScaledWidth();
        int factor3 = scale.getScaledHeight();
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3 || buffer == null
                || blurShader == null) {
            initFboAndShader();
        }
        lastScale = factor;
        lastScaleWidth = factor2;
        lastScaleHeight = factor3;

        if (OpenGlHelper.isFramebufferEnabled()) {

            buffer.framebufferClear();

            /*GL11.glScissor(x * factor, (mc.displayHeight - (y * factor) - height * factor), width * factor,
                  (height) * factor);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);*/
            MultipleGLScissor scissor = new MultipleGLScissor(x * factor, (mc.displayHeight - (y * factor) - height * factor), width * factor,
                    (height) * factor);

            setBlurConfigs(intensity, blurWidth, blurHeight);
            buffer.bindFramebuffer(true);
            blurShader.loadShaderGroup(mc.timer.renderPartialTicks);

            mc.getFramebuffer().bindFramebuffer(true);

            //GL11.glDisable(GL11.GL_SCISSOR_TEST);
            scissor.destroy();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO,
                    GL11.GL_ONE);
            buffer.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false);
            GlStateManager.disableBlend();
            GL11.glScalef(factor, factor, 0);

        } else {
            initFboAndShader();
        }
    }

    void phosphorArea(float x, float y, float width, float height,float multiplier) {
        ScaledResolution scale = RenderUtils.SCALED_RES;
        int factor = scale.getScaleFactor();
        int factor2 = scale.getScaledWidth();
        int factor3 = scale.getScaledHeight();
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3 || buffer == null || blurShader == null || !blurShader.getShaderGroupName().equals(shader.toString())) {
            initFboAndShader();
        }
        lastScale = factor;
        lastScaleWidth = factor2;
        lastScaleHeight = factor3;

        if (OpenGlHelper.isFramebufferEnabled()) {

            buffer.framebufferClear();

            /*GL11.glScissor(x * factor, (mc.displayHeight - (y * factor) - height * factor), width * factor,
                  (height) * factor);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);*/
            MultipleGLScissor scissor = new MultipleGLScissor(x * factor, (mc.displayHeight - (y * factor) - height * factor), width * factor, (height) * factor);
            setPhosphorConfigs(multiplier);
            buffer.bindFramebuffer(true);
            blurShader.loadShaderGroup(mc.timer.renderPartialTicks);

            mc.getFramebuffer().bindFramebuffer(true);

            //GL11.glDisable(GL11.GL_SCISSOR_TEST);
            scissor.destroy();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
            buffer.framebufferRenderExt(mc.displayWidth, mc.displayHeight,false);
            GlStateManager.disableBlend();
            GL11.glScalef(factor, factor, 0);
        }
    }

    void effectArea(float x, float y, float width, float height) {
        ScaledResolution scale = RenderUtils.SCALED_RES;
        int factor = scale.getScaleFactor();
        int factor2 = scale.getScaledWidth();
        int factor3 = scale.getScaledHeight();
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3 || buffer == null || blurShader == null || !blurShader.getShaderGroupName().equals(shader.toString())) {
            initFboAndShader();
        }
        lastScale = factor;
        lastScaleWidth = factor2;
        lastScaleHeight = factor3;

        if (OpenGlHelper.isFramebufferEnabled()) {

            buffer.framebufferClear();

            /*GL11.glScissor(x * factor, (mc.displayHeight - (y * factor) - height * factor), width * factor,
                  (height) * factor);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);*/
            MultipleGLScissor scissor = new MultipleGLScissor(x * factor, (mc.displayHeight - (y * factor) - height * factor), width * factor,
                    (height) * factor);
            buffer.bindFramebuffer(true);
            blurShader.loadShaderGroup(mc.timer.renderPartialTicks);

            mc.getFramebuffer().bindFramebuffer(true);

            //GL11.glDisable(GL11.GL_SCISSOR_TEST);
            scissor.destroy();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO,
                    GL11.GL_ONE);
            buffer.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false);
            GlStateManager.disableBlend();
            GL11.glScalef(factor, factor, 0);

        }
    }

    public static void blurArea(float x, float y, float width, float height, float intensity) {
        ScaledResolution scale = RenderUtils.SCALED_RES;
        int factor = scale.getScaleFactor();
        int factor2 = scale.getScaledWidth();
        int factor3 = scale.getScaledHeight();
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3 || buffer == null
                || blurShader == null) {
            initFboAndShader();
        }
        lastScale = factor;
        lastScaleWidth = factor2;
        lastScaleHeight = factor3;

        buffer.framebufferClear();

        /*GL11.glScissor(x * factor, (mc.displayHeight - (y * factor) - height * factor), width * factor,
                (height) * factor);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);*/
        MultipleGLScissor scissor = new MultipleGLScissor(x * factor, (mc.displayHeight - (y * factor) - height * factor), width * factor,
                (height) * factor);

        setBlurConfigs(intensity, 1, 0);
        buffer.bindFramebuffer(true);
        blurShader.loadShaderGroup(mc.timer.renderPartialTicks);

        mc.getFramebuffer().bindFramebuffer(true);

        //GL11.glDisable(GL11.GL_SCISSOR_TEST);
        scissor.destroy();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
        buffer.framebufferRenderExt(mc.displayWidth, mc.displayHeight, false);
        GlStateManager.disableBlend();
        GL11.glScalef(factor, factor, 0);
    }

    public static void blurAreaBoarder(int x, int y, int width, int height, float intensity, float blurWidth,
                                       float blurHeight) {
        ScaledResolution scale = RenderUtils.SCALED_RES;
        int factor = scale.getScaleFactor();
        int factor2 = scale.getScaledWidth();
        int factor3 = scale.getScaledHeight();
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3 || buffer == null
                || blurShader == null) {
            initFboAndShader();
        }
        lastScale = factor;
        lastScaleWidth = factor2;
        lastScaleHeight = factor3;

        /*GL11.glScissor(x * factor, (mc.displayHeight - (y * factor) - height * factor), width * factor,
                height * factor);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        */
        MultipleGLScissor scissor = new MultipleGLScissor(x * factor, (mc.displayHeight - (y * factor) - height * factor), width * factor,
                (height) * factor);

        setBlurConfigs(intensity, blurWidth, blurHeight);
        buffer.bindFramebuffer(true);
        blurShader.loadShaderGroup(mc.timer.renderPartialTicks);

        mc.getFramebuffer().bindFramebuffer(true);

        //GL11.glDisable(GL11.GL_SCISSOR_TEST);
        scissor.destroy();
    }

    public static void blurAreaBoarder(int x, int y, int width, int height, float intensity) {
        ScaledResolution scale = RenderUtils.SCALED_RES;
        int factor = scale.getScaleFactor();
        int factor2 = scale.getScaledWidth();
        int factor3 = scale.getScaledHeight();
        if (lastScale != factor || lastScaleWidth != factor2 || lastScaleHeight != factor3 || buffer == null
                || blurShader == null) {
            initFboAndShader();
        }
        lastScale = factor;
        lastScaleWidth = factor2;
        lastScaleHeight = factor3;

        /*GL11.glScissor(x * factor, (mc.displayHeight - (y * factor) - height * factor), width * factor,
                (height) * factor);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        */
        MultipleGLScissor scissor = new MultipleGLScissor(x * factor, (mc.displayHeight - (y * factor) - height * factor), width * factor,
                (height) * factor);
        setBlurConfigs(intensity, 1, 0);
        buffer.bindFramebuffer(true);
        blurShader.loadShaderGroup(mc.timer.renderPartialTicks);

        mc.getFramebuffer().bindFramebuffer(true);

        //GL11.glDisable(GL11.GL_SCISSOR_TEST);
        scissor.destroy();
    }
}
