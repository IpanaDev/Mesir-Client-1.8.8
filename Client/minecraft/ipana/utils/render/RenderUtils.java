package ipana.utils.render;

import ipana.Ipana;
import ipana.managements.module.Modules;
import ipana.modules.render.XRay;
import ipana.utils.gl.GList;
import ipana.utils.shader.ShaderManager;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.*;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import optifine.Config;
import optifine.Reflector;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import shadersmod.client.Shaders;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;

public class RenderUtils {

    public static ScaledResolution SCALED_RES;
    private static Minecraft mc = Minecraft.getMinecraft();
    private static final ResourceLocation locationRainPng = new ResourceLocation("textures/environment/rain.png");
    private static final ResourceLocation locationSnowPng = new ResourceLocation("textures/environment/snow.png");

    public static EnumWorldBlockLayer fixBlockLayer(Block block, EnumWorldBlockLayer layer) {
        if (Config.isMipmaps()) {
            if (layer == EnumWorldBlockLayer.CUTOUT) {
                /*
                if (p_fixBlockLayer_1_ instanceof BlockRedstoneWire) {
                    return layer;
                }

                if (p_fixBlockLayer_1_ instanceof BlockCactus) {
                    return layer;
                }

                 */

                return EnumWorldBlockLayer.CUTOUT_MIPPED;
            }
        } else if (layer == EnumWorldBlockLayer.CUTOUT_MIPPED) {
            return EnumWorldBlockLayer.CUTOUT;
        }
        XRay xRay = Modules.X_RAY;
        if (xRay.isEnabled() && xRay.checkAllValidity(block)) {
            return EnumWorldBlockLayer.ORE;
        }

        return layer;
    }

    public static int rainbow(int delay) {
        double rainbowState = Math.ceil((System.currentTimeMillis() + delay) / 20.0);
        rainbowState%=360;
        return Color.getHSBColor((float) (rainbowState/360),0.5f,0.7f).getRGB();
    }
    public static Color rainbow(int delay,float s,float b) {
        double rainbowState = Math.ceil((System.currentTimeMillis() + delay) / 20.0);
        rainbowState%=360;
        return Color.getHSBColor((float) (rainbowState/360),s,b);
    }

    public static void drawHexagon(double x, double y, double width, double height, double lineWidth, Color color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GL11.glLineWidth((float) lineWidth);
        GlStateManager.color(color.getRed()/255f,color.getGreen()/255f,color.getBlue()/255f,color.getAlpha()/255f);
        GL11.glBegin(GL_LINE_STRIP);
        GL11.glVertex2d(x-1, y+height/4+1);
        GL11.glVertex2d(x+width/4, y);
        GL11.glVertex2d(x+width/2, y);
        GL11.glVertex2d(x+width/2+width/4+1, y+height/4+1);
        GL11.glVertex2d(x+width/2+width/4, y+height/4);
        GL11.glVertex2d(x+width/2+width/4, y+height/2);
        GL11.glVertex2d(x+width/2+width/4+1, y+height/2-1);
        GL11.glVertex2d(x+width/2, y+height/2+height/4);
        GL11.glVertex2d(x+width/4, y+height/2+height/4);
        GL11.glVertex2d(x-1, y+height/2-1);
        GL11.glVertex2d(x, y+height/2);
        GL11.glVertex2d(x, y+height/4);
        GL11.glEnd();


        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void drawGradientRect(double left, double top, double right, double bottom, int startColor, int endColor)
    {
        double zLevel = 0;
        float f = (float)(startColor >> 24 & 255) / 255.0F;
        float f1 = (float)(startColor >> 16 & 255) / 255.0F;
        float f2 = (float)(startColor >> 8 & 255) / 255.0F;
        float f3 = (float)(startColor & 255) / 255.0F;
        float f4 = (float)(endColor >> 24 & 255) / 255.0F;
        float f5 = (float)(endColor >> 16 & 255) / 255.0F;
        float f6 = (float)(endColor >> 8 & 255) / 255.0F;
        float f7 = (float)(endColor & 255) / 255.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(right, top, zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, top, zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(right, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
    public static void drawGradientRect(double left, double top, double right, double bottom, int startColor, int endColor, int sS, int eS)
    {
        double zLevel = 0;
        float f = (float)(startColor >> 24 & 255) / sS;
        float f1 = (float)(startColor >> 16 & 255) / sS;
        float f2 = (float)(startColor >> 8 & 255) / sS;
        float f3 = (float)(startColor & 255) / sS;
        float f4 = (float)(endColor >> 24 & 255) / eS;
        float f5 = (float)(endColor >> 16 & 255) / eS;
        float f6 = (float)(endColor >> 8 & 255) / eS;
        float f7 = (float)(endColor & 255) / eS;
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(right, top, zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, top, zLevel).color(f1, f2, f3, f).endVertex();
        worldrenderer.pos(left, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        worldrenderer.pos(right, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }
    public static void drawFixedRect(double left, double top, double right, double bottom, Color color) {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770,771,0,1);
        GlStateManager.depthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glColor4f(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() / 255.0F);
        Gui.drawRect(left,top,right,bottom,color.getRGB());
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
        GlStateManager.popMatrix();
    }
    public static void drawFixedRect(double left, double top, double right, double bottom, float[] colors) {
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770,771,0,1);
        GlStateManager.depthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glColor4f(colors[0], colors[1], colors[2], colors[3]);
        Gui.drawRect(left,top,right,bottom,new int[]{(int) (colors[0]*255), (int) (colors[1]*255), (int) (colors[2]*255), (int) (colors[3]*255)});
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
        GlStateManager.popMatrix();
    }
    public static void drawFixedRect(double left, double top, double right, double bottom, int[] color) {
        GlStateManager.pushMatrix();
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GlStateManager.enableBlend();
        GL11.glDisable(GL_TEXTURE_2D);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(GL_LINE_SMOOTH);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
        GL11.glColor4f(color[0]/255f, color[1]/255f, color[2]/255f, color[3]/255f);
        Gui.drawRect(left,top,right,bottom,color);
        GL11.glEnable(GL_TEXTURE_2D);
        GlStateManager.disableBlend();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL_LINE_SMOOTH);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
        GlStateManager.popMatrix();
    }
    public static void drawFixedRectWH(double x, double y, double width, double height, Color color) {
        drawFixedRect(x,y,x+width,y+height,color);
    }
    public static void drawBlendedRect(double left, double top, double right, double bottom,int srcFactor, int dstFactor, int srcFactorAlpha, int dstFactorAlpha, Color color) {
        if (left < right) {
            double i = left;
            left = right;
            right = i;
        }

        if (top < bottom) {
            double j = top;
            top = bottom;
            bottom = j;
        }
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(srcFactor, dstFactor, srcFactorAlpha, dstFactorAlpha);
        GlStateManager.color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.disableBlend();
    }


    public static void drawImage(double x, double y, double xWidth, double yWidth, ResourceLocation image) {
        double par1 = x + xWidth;
        double par2 = y + yWidth;
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);
        GlStateManager.disableAlpha();
        GlStateManager.enableAlpha();
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, par2, 0.0D).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos(par1, par2, 0.0D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos(par1, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
    }
    public static void drawImage(double x, double y, double xWidth, double yWidth, int textureId) {
        double par1 = x + xWidth;
        double par2 = y + yWidth;
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1F);
        GlStateManager.disableAlpha();
        GlStateManager.enableAlpha();
        GlStateManager.bindTexture(textureId);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, par2, 0.0D).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos(par1, par2, 0.0D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos(par1, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
    }
    public static void drawImage(double x, double y, double xWidth, double yWidth,Color color, ResourceLocation image) {
        double par1 = x + xWidth;
        double par2 = y + yWidth;
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f);
        GlStateManager.disableAlpha();
        GlStateManager.enableAlpha();
        Minecraft.getMinecraft().getTextureManager().bindTexture(image);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, par2, 0.0D).tex(0.0D, 1.0D).endVertex();
        worldrenderer.pos(par1, par2, 0.0D).tex(1.0D, 1.0D).endVertex();
        worldrenderer.pos(par1, y, 0.0D).tex(1.0D, 0.0D).endVertex();
        worldrenderer.pos(x, y, 0.0D).tex(0.0D, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableAlpha();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
    }
    public static void drawRoundedRect(double x, double y, double width, double height, double radius, Color color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        double x1 = x + width;
        double y1 = y + height;
        GL11.glPushAttrib(0);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        if (color != null) {
            GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        }
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glBegin(GL11.GL_POLYGON);


        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x + radius + (MathHelper.sin((i * Math.PI / 180)) * (radius * -1)),
                    y + radius + (MathHelper.cos((i * Math.PI / 180)) * (radius * -1)));
        }

        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x + radius + (MathHelper.sin((i * Math.PI / 180)) * (radius * -1)),
                    y1 - radius + (MathHelper.cos((i * Math.PI / 180)) * (radius * -1)));
        }

        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x1 - radius + (MathHelper.sin((i * Math.PI / 180)) * radius),
                    y1 - radius + (MathHelper.cos((i * Math.PI / 180)) * radius));
        }

        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x1 - radius + (MathHelper.sin((i * Math.PI / 180)) * radius),
                    y + radius + (MathHelper.cos((i * Math.PI / 180)) * radius));
        }

        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glPopAttrib();
        GL11.glColor4f(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    public static void drawRoundedRect(double x, double y, double width, double height, double radius, Color color, float customAlpha) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        double x1 = x + width;
        double y1 = y + height;
        GL11.glPushAttrib(0);

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        if (color != null) {
            GL11.glColor4f(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, customAlpha);
        }
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        GL11.glBegin(GL11.GL_POLYGON);


        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x + radius + +(MathHelper.sin((i * Math.PI / 180)) * (radius * -1)),
                    y + radius + (MathHelper.cos((i * Math.PI / 180)) * (radius * -1)));
        }

        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x + radius + (MathHelper.sin((i * Math.PI / 180)) * (radius * -1)),
                    y1 - radius + (MathHelper.cos((i * Math.PI / 180)) * (radius * -1)));
        }

        for (int i = 0; i <= 90; i += 3) {
            GL11.glVertex2d(x1 - radius + (MathHelper.sin((i * Math.PI / 180)) * radius),
                    y1 - radius + (MathHelper.cos((i * Math.PI / 180)) * radius));
        }

        for (int i = 90; i <= 180; i += 3) {
            GL11.glVertex2d(x1 - radius + (MathHelper.sin((i * Math.PI / 180)) * radius),
                    y + radius + (MathHelper.cos((i * Math.PI / 180)) * radius));
        }

        GL11.glEnd();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glPopAttrib();
        GL11.glColor4f(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    public static void drawRoundedGradientRect(double x, double y, double width, double height, double r, int cornerAngle, Color color, Color color2) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(7425);
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        float xR = color2.getRed() / 255f;
        float xG = color2.getGreen() / 255f;
        float xB = color2.getBlue() / 255f;
        float xA = color2.getAlpha() / 255f;
        float yR = color.getRed() / 255f;
        float yG = color.getGreen() / 255f;
        float yB = color.getBlue() / 255f;
        float yA = color.getAlpha() / 255f;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        //TOP
        worldRenderer.pos(x+r, y+r, 0).color(yR, yG, yB, yA).endVertex();
        worldRenderer.pos(x+width-r, y+r, 0).color(yR, yG, yB, yA).endVertex();
        worldRenderer.pos(x+width-r, y, 0).color(xR, xG, xB, xA).endVertex();
        worldRenderer.pos(x+r, y, 0).color(xR, xG, xB, xA).endVertex();
        //LEFT
        worldRenderer.pos(x, y+height-r, 0).color(xR, xG, xB, xA).endVertex();
        worldRenderer.pos(x+r, y+height-r, 0).color(yR, yG, yB, yA).endVertex();
        worldRenderer.pos(x+r, y+r, 0).color(yR, yG, yB, yA).endVertex();
        worldRenderer.pos(x, y+r, 0).color(xR, xG, xB, xA).endVertex();
        //BOTTOM
        worldRenderer.pos(x+r, y+height, 0).color(xR, xG, xB, xA).endVertex();
        worldRenderer.pos(x+width-r, y+height, 0).color(xR, xG, xB, xA).endVertex();
        worldRenderer.pos(x+width-r, y+height-r, 0).color(yR, yG, yB, yA).endVertex();
        worldRenderer.pos(x+r, y+height-r, 0).color(yR, yG, yB, yA).endVertex();
        //RIGHT
        worldRenderer.pos(x+width-r, y+height-r, 0).color(yR, yG, yB, yA).endVertex();
        worldRenderer.pos(x+width, y+height-r, 0).color(xR, xG, xB, xA).endVertex();
        worldRenderer.pos(x+width, y+r, 0).color(xR, xG, xB, xA).endVertex();
        worldRenderer.pos(x+width-r, y+r, 0).color(yR, yG, yB, yA).endVertex();
        //INNER
        worldRenderer.pos(x+r, y+height-r, 0).color(yR, yG, yB, yA).endVertex();
        worldRenderer.pos(x+width-r, y+height-r, 0).color(yR, yG, yB, yA).endVertex();
        worldRenderer.pos(x+width-r, y+r, 0).color(yR, yG, yB, yA).endVertex();
        worldRenderer.pos(x+r, y+r, 0).color(yR, yG, yB, yA).endVertex();
        tessellator.draw();

        //CORNERS
        worldRenderer.begin(GL_TRIANGLES, DefaultVertexFormats.POSITION_COLOR);
        double lastCos = 0;
        double lastSin = 0;
        //TOP LEFT CORNER
        for (double i = 180; i >= 90; i-=cornerAngle) {
            double radian = Math.toRadians(i);
            double cos = r*MathHelper.cos(radian);
            double sin = r*MathHelper.sin(radian);
            worldRenderer.pos(x+r+lastCos, y+r - lastSin, 0).color(xR, xG, xB, xA).endVertex();
            worldRenderer.pos(x+r, y+r, 0).color(yR, yG, yB, yA).endVertex();
            worldRenderer.pos(x + r + cos, y + r - sin, 0).color(xR, xG, xB, xA).endVertex();
            lastCos = cos;
            lastSin = sin;
        }
        lastCos = 0;
        lastSin = 0;
        //TOP RIGHT CORNER
        for (int i = 0; i <= 90; i+=cornerAngle) {
            double radian = Math.toRadians(i);
            double cos = r*MathHelper.cos(radian);
            double sin = r*MathHelper.sin(radian);
            worldRenderer.pos(x+width-r, y+r, 0).color(yR, yG, yB, yA).endVertex();
            worldRenderer.pos(x+width-r+lastCos, y+r - lastSin, 0).color(xR, xG, xB, xA).endVertex();
            worldRenderer.pos(x+width-r + cos, y + r - sin, 0).color(xR, xG, xB, xA).endVertex();
            lastCos = cos;
            lastSin = sin;
        }
        lastCos = 0;
        lastSin = 0;
        //BOTTOM LEFT CORNER
        for (int i = 180; i <= 270; i+=cornerAngle) {
            double radian = Math.toRadians(i);
            double cos = r*MathHelper.cos(radian);
            double sin = r*MathHelper.sin(radian);
            worldRenderer.pos(x + r + cos, y + height - r - sin, 0).color(xR, xG, xB, xA).endVertex();
            worldRenderer.pos(x+r, y+height-r, 0).color(yR, yG, yB, yA).endVertex();
            worldRenderer.pos(x+r+lastCos, y+height-r - lastSin, 0).color(xR, xG, xB, xA).endVertex();
            lastCos = cos;
            lastSin = sin;
        }
        lastCos = 0;
        lastSin = 0;
        //BOTTOM RIGHT CORNER
        for (int i = 270; i <= 360; i+=cornerAngle) {
            double radian = Math.toRadians(i);
            double cos = r*MathHelper.cos(radian);
            double sin = r*MathHelper.sin(radian);
            worldRenderer.pos(x + width - r + cos, y + height - r - sin, 0).color(xR, xG, xB, xA).endVertex();
            worldRenderer.pos(x + width - r, y+height-r, 0).color(yR, yG, yB, yA).endVertex();
            worldRenderer.pos(x + width - r+lastCos, y+height-r - lastSin, 0).color(xR, xG, xB, xA).endVertex();
            lastCos = cos;
            lastSin = sin;
        }
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.enableTexture2D();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    public static void drawBorderedRect(final double left, final double top, final double right, final double bottom, final double borderWidth, final int insideColor, final int borderColor,
                                        final boolean borderIncludedInBounds) {
        Gui.drawRect(left - (!borderIncludedInBounds ? borderWidth : 0), top - (!borderIncludedInBounds ? borderWidth : 0), right + (!borderIncludedInBounds ? borderWidth : 0),
                bottom + (!borderIncludedInBounds ? borderWidth : 0), borderColor);
        Gui.drawRect(left + (borderIncludedInBounds ? borderWidth : 0), top + (borderIncludedInBounds ? borderWidth : 0), right - ((borderIncludedInBounds ? borderWidth : 0)),
                bottom - ((borderIncludedInBounds ? borderWidth : 0)), insideColor);
    }

    public static void shadowAround(double x, double y, double width, double height, Color startColor, Color endColor) {
        double zLevel = 0;
        double gradientSize = 10;
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        //X: -1 Y: 0
        worldrenderer.pos(x, y, zLevel).color(startColor).endVertex();
        worldrenderer.pos(x-gradientSize, y, zLevel).color(endColor).endVertex();
        worldrenderer.pos(x-gradientSize, y+height, zLevel).color(endColor).endVertex();
        worldrenderer.pos(x, y+height, zLevel).color(startColor).endVertex();

        //X: 1 Y: 0
        worldrenderer.pos(x+width+gradientSize, y, zLevel).color(endColor).endVertex();
        worldrenderer.pos(x+width, y, zLevel).color(startColor).endVertex();
        worldrenderer.pos(x+width, y+height, zLevel).color(startColor).endVertex();
        worldrenderer.pos(x+width+gradientSize, y+height, zLevel).color(endColor).endVertex();

        //X: 0 Y: -1
        worldrenderer.pos(x+width, y-gradientSize, zLevel).color(endColor).endVertex();
        worldrenderer.pos(x, y-gradientSize, zLevel).color(endColor).endVertex();
        worldrenderer.pos(x, y, zLevel).color(startColor).endVertex();
        worldrenderer.pos(x+width, y, zLevel).color(startColor).endVertex();

        //X: 0 Y: 1
        worldrenderer.pos(x+width, y+height, zLevel).color(startColor).endVertex();
        worldrenderer.pos(x, y+height, zLevel).color(startColor).endVertex();
        worldrenderer.pos(x, y+height+gradientSize, zLevel).color(endColor).endVertex();
        worldrenderer.pos(x+width, y+height+gradientSize, zLevel).color(endColor).endVertex();

        //X: -1 Y: -1
        worldrenderer.pos(x, y-gradientSize, zLevel).color(endColor).endVertex();
        worldrenderer.pos(x-gradientSize, y-gradientSize, zLevel).color(endColor).endVertex();
        worldrenderer.pos(x-gradientSize, y, zLevel).color(endColor).endVertex();
        worldrenderer.pos(x, y, zLevel).color(startColor).endVertex();

        //X: 1 Y: -1
        worldrenderer.pos(x+width+gradientSize, y, zLevel).color(endColor).endVertex();
        worldrenderer.pos(x+width, y-gradientSize, zLevel).color(endColor).endVertex();
        worldrenderer.pos(x+width, y, zLevel).color(startColor).endVertex();
        worldrenderer.pos(x+width+gradientSize, y-gradientSize, zLevel).color(endColor).endVertex();

        //X: 1 Y: 1
        worldrenderer.pos(x+width+gradientSize, y+height, zLevel).color(endColor).endVertex();
        worldrenderer.pos(x+width, y+height, zLevel).color(startColor).endVertex();
        worldrenderer.pos(x+width, y+height+gradientSize, zLevel).color(endColor).endVertex();
        worldrenderer.pos(x+width+gradientSize, y+height+gradientSize, zLevel).color(endColor).endVertex();

        //X: -1 Y: 1
        worldrenderer.pos(x, y+height, zLevel).color(startColor).endVertex();
        worldrenderer.pos(x-gradientSize, y+height+gradientSize, zLevel).color(endColor).endVertex();
        worldrenderer.pos(x-gradientSize, y+height, zLevel).color(endColor).endVertex();
        worldrenderer.pos(x, y+height+gradientSize, zLevel).color(endColor).endVertex();

        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static void drawGradient(double x, double y, double width, double height, Color startColor, Color endColor, Direction direction) {
        double zLevel = 0;
        double gradientSize = 10;
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        switch (direction) {
            case LEFT -> {
                worldrenderer.pos(x, y, zLevel).color(startColor).endVertex();
                worldrenderer.pos(x-gradientSize, y, zLevel).color(endColor).endVertex();
                worldrenderer.pos(x-gradientSize, y+height, zLevel).color(endColor).endVertex();
                worldrenderer.pos(x, y+height, zLevel).color(startColor).endVertex();
            }
            case RIGHT -> {
                worldrenderer.pos(x+width+gradientSize, y, zLevel).color(endColor).endVertex();
                worldrenderer.pos(x+width, y, zLevel).color(startColor).endVertex();
                worldrenderer.pos(x+width, y+height, zLevel).color(startColor).endVertex();
                worldrenderer.pos(x+width+gradientSize, y+height, zLevel).color(endColor).endVertex();
            }
            case UP -> {
                worldrenderer.pos(x+width, y-gradientSize, zLevel).color(endColor).endVertex();
                worldrenderer.pos(x, y-gradientSize, zLevel).color(endColor).endVertex();
                worldrenderer.pos(x, y, zLevel).color(startColor).endVertex();
                worldrenderer.pos(x+width, y, zLevel).color(startColor).endVertex();
            }
            case DOWN -> {
                worldrenderer.pos(x+width, y+height, zLevel).color(startColor).endVertex();
                worldrenderer.pos(x, y+height, zLevel).color(startColor).endVertex();
                worldrenderer.pos(x, y+height+gradientSize, zLevel).color(endColor).endVertex();
                worldrenderer.pos(x+width, y+height+gradientSize, zLevel).color(endColor).endVertex();
            }
        }
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
    public static void drawOutlineBox(AxisAlignedBB p_181561_0_) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color((float) Ipana.getClientColor().getRed()/255, (float)Ipana.getClientColor().getGreen()/255, (float)Ipana.getClientColor().getBlue()/255, 1);
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(3, DefaultVertexFormats.POSITION);
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        tessellator.draw();
        worldrenderer.begin(1, DefaultVertexFormats.POSITION);
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.minZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.maxX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.minY, p_181561_0_.maxZ).endVertex();
        worldrenderer.pos(p_181561_0_.minX, p_181561_0_.maxY, p_181561_0_.maxZ).endVertex();
        tessellator.draw();
    }
    public static ResourceLocation downloadUrl(String url) {
        return Ipana.dynamicTextureManager.getTexture(url, url);
    }

    public enum WeatherType {
        Snow,Rain
    }

    public enum Direction {
        UP,DOWN,RIGHT,LEFT
    }
}
