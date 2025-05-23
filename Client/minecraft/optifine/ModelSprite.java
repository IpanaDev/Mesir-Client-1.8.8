package optifine;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

public class ModelSprite
{
    private ModelRenderer modelRenderer = null;
    private int textureOffsetX = 0;
    private int textureOffsetY = 0;
    public float posX = 0.0F;
    public float posY = 0.0F;
    public float posZ = 0.0F;
    private int sizeX = 0;
    private int sizeY = 0;
    private int sizeZ = 0;
    private float sizeAdd = 0.0F;
    private float minU = 0.0F;
    private float minV = 0.0F;
    private float maxU = 0.0F;
    private float maxV = 0.0F;

    public ModelSprite(ModelRenderer p_i67_1_, int p_i67_2_, int p_i67_3_, float p_i67_4_, float p_i67_5_, float p_i67_6_, int p_i67_7_, int p_i67_8_, int p_i67_9_, float p_i67_10_) {
        this.modelRenderer = p_i67_1_;
        this.textureOffsetX = p_i67_2_;
        this.textureOffsetY = p_i67_3_;
        this.posX = p_i67_4_;
        this.posY = p_i67_5_;
        this.posZ = p_i67_6_;
        this.sizeX = p_i67_7_;
        this.sizeY = p_i67_8_;
        this.sizeZ = p_i67_9_;
        this.sizeAdd = p_i67_10_;
        this.minU = (float)p_i67_2_ / p_i67_1_.textureWidth;
        this.minV = (float)p_i67_3_ / p_i67_1_.textureHeight;
        this.maxU = (float)(p_i67_2_ + p_i67_7_) / p_i67_1_.textureWidth;
        this.maxV = (float)(p_i67_3_ + p_i67_8_) / p_i67_1_.textureHeight;
    }
    public void render(Tessellator p_render_1_, float scale) {
        render(true, p_render_1_, scale);
    }

    public void render(boolean autoDraw, Tessellator tessellator, float scale) {
        //GlStateManager.translate(this.posX * scale, this.posY * scale, this.posZ * scale);
        float f = this.minU;
        float f1 = this.maxU;
        float f2 = this.minV;
        float f3 = this.maxV;

        if (this.modelRenderer.mirror) {
            f = this.maxU;
            f1 = this.minU;
        }

        if (this.modelRenderer.mirrorV) {
            f2 = this.maxV;
            f3 = this.minV;
        }

        renderItemIn2DFast(tessellator, autoDraw, f, f2, f1, f3, this.sizeX, this.sizeY, scale * (float)this.sizeZ, this.modelRenderer.textureWidth, this.modelRenderer.textureHeight);
        //GlStateManager.translate(-this.posX * scale, -this.posY * scale, -this.posZ * scale);
    }
    public static void renderItemIn2DFast(Tessellator tessellator, boolean autoDraw, float minU, float minV, float maxU, float maxV, int sizeX, int sizeY, float scale, float textureWidth, float textureHeight) {
        if (scale < 6.25E-4F) {
            scale = 6.25E-4F;
        }

        float f = maxU - minU;
        float f1 = maxV - minV;
        double d0 = (MathHelper.abs(f) * (textureWidth / 16.0F));
        double d1 = (MathHelper.abs(f1) * (textureHeight / 16.0F));
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        if (autoDraw) {
            worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        }
        worldrenderer.pos(0.0D, d1, 0.0D).tex(minU, (double)maxV).normal(0,0,-1).endVertex();
        worldrenderer.pos(d0, d1, 0.0D).tex(maxU, (double)maxV).normal(0,0,-1).endVertex();
        worldrenderer.pos(d0, 0.0D, 0.0D).tex(maxU, (double)minV).normal(0,0,-1).endVertex();
        worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(minU, (double)minV).normal(0,0,-1).endVertex();

        worldrenderer.pos(0.0D, 0.0D, scale).tex(minU, (double)minV).normal(0,0,1).endVertex();
        worldrenderer.pos(d0, 0.0D, scale).tex(maxU, (double)minV).normal(0,0,1).endVertex();
        worldrenderer.pos(d0, d1, scale).tex(maxU, (double)maxV).normal(0,0,1).endVertex();
        worldrenderer.pos(0.0D, d1, scale).tex(minU, (double)maxV).normal(0,0,1).endVertex();
        float f2 = 0.5F * f / (float)sizeX;
        float f3 = 0.5F * f1 / (float)sizeY;
        for (int i = 0; i < sizeX; ++i) {
            float f4 = (float)i / (float)sizeX;
            float f5 = minU + f * f4 + f2;
            worldrenderer.pos((double)f4 * d0, d1, scale).tex(f5, (double)maxV).normal(-1,0,0).endVertex();
            worldrenderer.pos((double)f4 * d0, d1, 0.0D).tex(f5, (double)maxV).normal(-1,0,0).endVertex();
            worldrenderer.pos((double)f4 * d0, 0.0D, 0.0D).tex(f5, (double)minV).normal(-1,0,0).endVertex();
            worldrenderer.pos((double)f4 * d0, 0.0D, scale).tex(f5, (double)minV).normal(-1,0,0).endVertex();
        }
        for (int j = 0; j < sizeX; ++j) {
            float f7 = (float)j / (float)sizeX;
            float f10 = minU + f * f7 + f2;
            float f6 = f7 + 1.0F / (float)sizeX;
            worldrenderer.pos((double)f6 * d0, 0.0D, scale).tex(f10, (double)minV).normal(1,0,0).endVertex();
            worldrenderer.pos((double)f6 * d0, 0.0D, 0.0D).tex(f10, (double)minV).normal(1,0,0).endVertex();
            worldrenderer.pos((double)f6 * d0, d1, 0.0D).tex(f10, (double)maxV).normal(1,0,0).endVertex();
            worldrenderer.pos((double)f6 * d0, d1, scale).tex(f10, (double)maxV).normal(1,0,0).endVertex();
        }
        for (int k = 0; k < sizeY; ++k) {
            float f8 = (float)k / (float)sizeY;
            float f11 = minV + f1 * f8 + f3;
            float f13 = f8 + 1.0F / (float)sizeY;
            worldrenderer.pos(0.0D, (double)f13 * d1, scale).tex(minU, (double)f11).normal(0,1,0).endVertex();
            worldrenderer.pos(d0, (double)f13 * d1, scale).tex(maxU, (double)f11).normal(0,1,0).endVertex();
            worldrenderer.pos(d0, (double)f13 * d1, 0.0D).tex(maxU, (double)f11).normal(0,1,0).endVertex();
            worldrenderer.pos(0.0D, (double)f13 * d1, 0.0D).tex(minU, (double)f11).normal(0,1,0).endVertex();
        }
        for (int l = 0; l < sizeY; ++l) {
            float f9 = (float)l / (float)sizeY;
            float f12 = minV + f1 * f9 + f3;
            worldrenderer.pos(d0, (double)f9 * d1, scale).tex(maxU, (double)f12).normal(0,-1,0).endVertex();
            worldrenderer.pos(0.0D, (double)f9 * d1, scale).tex(minU, (double)f12).normal(0,-1,0).endVertex();
            worldrenderer.pos(0.0D, (double)f9 * d1, 0.0D).tex(minU, (double)f12).normal(0,-1,0).endVertex();
            worldrenderer.pos(d0, (double)f9 * d1, 0.0D).tex(maxU, (double)f12).normal(0,-1,0).endVertex();
        }
        if (autoDraw) {
            tessellator.draw();
        }
    }
    public static void renderItemIn2D(Tessellator tessellator, float minU, float minV, float maxU, float maxV, int sizeX, int sizeY, float p_renderItemIn2D_7_, float textureWidth, float textureHeight) {
        if (p_renderItemIn2D_7_ < 6.25E-4F) {
            p_renderItemIn2D_7_ = 6.25E-4F;
        }

        float f = maxU - minU;
        float f1 = maxV - minV;
        double d0 = (MathHelper.abs(f) * (textureWidth / 16.0F));
        double d1 = (MathHelper.abs(f1) * (textureHeight / 16.0F));
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GL11.glNormal3f(0.0F, 0.0F, -1.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(0.0D, d1, 0.0D).tex(minU, (double)maxV).endVertex();
        worldrenderer.pos(d0, d1, 0.0D).tex(maxU, (double)maxV).endVertex();
        worldrenderer.pos(d0, 0.0D, 0.0D).tex(maxU, (double)minV).endVertex();
        worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(minU, (double)minV).endVertex();
        tessellator.draw();
        GL11.glNormal3f(0.0F, 0.0F, 1.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(0.0D, 0.0D, p_renderItemIn2D_7_).tex(minU, (double)minV).endVertex();
        worldrenderer.pos(d0, 0.0D, p_renderItemIn2D_7_).tex(maxU, (double)minV).endVertex();
        worldrenderer.pos(d0, d1, p_renderItemIn2D_7_).tex(maxU, (double)maxV).endVertex();
        worldrenderer.pos(0.0D, d1, p_renderItemIn2D_7_).tex(minU, (double)maxV).endVertex();
        tessellator.draw();
        float f2 = 0.5F * f / (float)sizeX;
        float f3 = 0.5F * f1 / (float)sizeY;
        GL11.glNormal3f(-1.0F, 0.0F, 0.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

        for (int i = 0; i < sizeX; ++i)
        {
            float f4 = (float)i / (float)sizeX;
            float f5 = minU + f * f4 + f2;
            worldrenderer.pos((double)f4 * d0, d1, p_renderItemIn2D_7_).tex(f5, (double)maxV).endVertex();
            worldrenderer.pos((double)f4 * d0, d1, 0.0D).tex(f5, (double)maxV).endVertex();
            worldrenderer.pos((double)f4 * d0, 0.0D, 0.0D).tex(f5, (double)minV).endVertex();
            worldrenderer.pos((double)f4 * d0, 0.0D, p_renderItemIn2D_7_).tex(f5, (double)minV).endVertex();
        }

        tessellator.draw();
        GL11.glNormal3f(1.0F, 0.0F, 0.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

        for (int j = 0; j < sizeX; ++j)
        {
            float f7 = (float)j / (float)sizeX;
            float f10 = minU + f * f7 + f2;
            float f6 = f7 + 1.0F / (float)sizeX;
            worldrenderer.pos((double)f6 * d0, 0.0D, p_renderItemIn2D_7_).tex(f10, (double)minV).endVertex();
            worldrenderer.pos((double)f6 * d0, 0.0D, 0.0D).tex(f10, (double)minV).endVertex();
            worldrenderer.pos((double)f6 * d0, d1, 0.0D).tex(f10, (double)maxV).endVertex();
            worldrenderer.pos((double)f6 * d0, d1, p_renderItemIn2D_7_).tex(f10, (double)maxV).endVertex();
        }

        tessellator.draw();
        GL11.glNormal3f(0.0F, 1.0F, 0.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

        for (int k = 0; k < sizeY; ++k)
        {
            float f8 = (float)k / (float)sizeY;
            float f11 = minV + f1 * f8 + f3;
            float f13 = f8 + 1.0F / (float)sizeY;
            worldrenderer.pos(0.0D, (double)f13 * d1, p_renderItemIn2D_7_).tex(minU, (double)f11).endVertex();
            worldrenderer.pos(d0, (double)f13 * d1, p_renderItemIn2D_7_).tex(maxU, (double)f11).endVertex();
            worldrenderer.pos(d0, (double)f13 * d1, 0.0D).tex(maxU, (double)f11).endVertex();
            worldrenderer.pos(0.0D, (double)f13 * d1, 0.0D).tex(minU, (double)f11).endVertex();
        }

        tessellator.draw();
        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

        for (int l = 0; l < sizeY; ++l)
        {
            float f9 = (float)l / (float)sizeY;
            float f12 = minV + f1 * f9 + f3;
            worldrenderer.pos(d0, (double)f9 * d1, p_renderItemIn2D_7_).tex(maxU, (double)f12).endVertex();
            worldrenderer.pos(0.0D, (double)f9 * d1, p_renderItemIn2D_7_).tex(minU, (double)f12).endVertex();
            worldrenderer.pos(0.0D, (double)f9 * d1, 0.0D).tex(minU, (double)f12).endVertex();
            worldrenderer.pos(d0, (double)f9 * d1, 0.0D).tex(maxU, (double)f12).endVertex();
        }

        tessellator.draw();
    }
}
