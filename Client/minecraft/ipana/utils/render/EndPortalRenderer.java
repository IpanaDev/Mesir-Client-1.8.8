package ipana.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.Random;

public class EndPortalRenderer {
	private int portalLength;
    private Color[] colors;
    private float[][] colorTable;
    private boolean colorGenerated;

    public EndPortalRenderer(int length, Color... colors) {
        this.portalLength = length;
        this.colors = colors;
        this.colorTable = new float[length][4];
    }

	private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
	private static final ResourceLocation END_PORTAL_SHADER = new ResourceLocation("safe/end_portal_alpha.png");
	private static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
	private static final Random random = new Random(31100L);
	private static final FloatBuffer modelView = GLAllocation.createDirectFloatBuffer(16);
	private static final FloatBuffer projection = GLAllocation.createDirectFloatBuffer(16);
	private static final FloatBuffer portal_buffer = GLAllocation.createDirectFloatBuffer(16);
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Timer timer = new Timer(0.001f);
	
	private FloatBuffer getEndFXBuffer(float f0, float f1, float f2, float f3) {
		portal_buffer.clear();
		portal_buffer.put(f0).put(f1).put(f2).put(f3);
		portal_buffer.flip();
		return portal_buffer;
	}

	public void renderEndPortalEffect3D(double x, double y, double z, EnumFacing... renderSides) {
		beginToRender();
		var timesToRender = getTimesToRender(x, y, 1);
		for (var i = 0; i < timesToRender; ++i) {
			GlStateManager.pushMatrix();
			var colorFactor = 2.0F / (18 - i);
			handleTextures(i);
			manipulateTextures(i);
			var tess = Tessellator.getInstance();
			var wr = tess.getWorldRenderer();

			var red = (random.nextFloat() * 0.5F + 0.1F) * colorFactor;
			var green = (random.nextFloat() * 0.5F + 0.4F) * colorFactor;
			var blue = (random.nextFloat() * 0.5F + 0.5F) * colorFactor;

			wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
			for (EnumFacing f : renderSides) {
				switch (f) {
					case SOUTH -> {
						wr.pos(x, y, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x + 1.0D, y, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x + 1.0D, y + 1.0D, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x, y + 1.0D, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
					}
					case NORTH -> {
						wr.pos(x, y + 1.0D, z).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x + 1.0D, y + 1.0D, z).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x + 1.0D, y, z).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x, y, z).color(red, green, blue, 1.0F).endVertex();
					}
					case EAST -> {
						wr.pos(x + 1.0D, y + 1.0D, z).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x + 1.0D, y + 1.0D, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x + 1.0D, y, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x + 1.0D, y, z).color(red, green, blue, 1.0F).endVertex();
					}
					case WEST -> {
						wr.pos(x, y, z).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x, y, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x, y + 1.0D, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x, y + 1.0D, z).color(red, green, blue, 1.0F).endVertex();
					}
					case DOWN -> {
						wr.pos(x, y, z).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x + 1.0D, y, z).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x + 1.0D, y, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x, y, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
					}
					case UP -> {
						wr.pos(x, y + 1.0D, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x + 1.0D, y + 1.0D, z + 1.0D).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x + 1.0D, y + 1.0D, z).color(red, green, blue, 1.0F).endVertex();
						wr.pos(x, y + 1.0D, z).color(red, green, blue, 1.0F).endVertex();
					}
				}
			}
			tess.draw();

			endTextureRendering();
		}
		endRender(true);
	}

	public void renderEndPortalEffect2D(double x, double y, double width, double height) {
        timer.updateTimer(0.02f, System.nanoTime());
		if (!colorGenerated) {
            int colorIndex = 1;
            colorTable[0][0] = 0f;
            colorTable[0][1] = 0f;
            colorTable[0][2] = 0f;
            colorTable[0][3] = 0f;
            float red = colors[0].getRed()/255f;
            float green = colors[0].getGreen()/255f;
            float blue = colors[0].getBlue()/255f;
            float alpha = colors[0].getAlpha()/255f;
            int bokIndir = colorTable.length/(colors.length-1);
            for (int i = 1; i < colorTable.length; i++) {
                float[] starColors = colorTable[i];
                starColors[0] = red;
                starColors[1] = green;
                starColors[2] = blue;
                starColors[3] = alpha;

                Color nextColor = colors[colorIndex];
                Color previousColor = colors[colorIndex-1];
                red += (nextColor.getRed()-previousColor.getRed())/255f/bokIndir;
                green += (nextColor.getGreen()-previousColor.getGreen())/255f/bokIndir;
                blue += (nextColor.getBlue()-previousColor.getBlue())/255f/bokIndir;
                alpha += (nextColor.getAlpha()-previousColor.getAlpha())/255f/bokIndir;
                double redDiff = red - nextColor.getRed()/255f;
                double greenDiff = green - nextColor.getGreen()/255f;
                double blueDiff = blue - nextColor.getBlue()/255f;
                double alphaDiff = alpha - nextColor.getAlpha()/255f;
                if (redDiff > -1E-7 && redDiff < 1E-7 && greenDiff > -1E-7 && greenDiff < 1E-7 && blueDiff > -1E-7 && blueDiff < 1E-7 && alphaDiff > -1E-7 && alphaDiff < 1E-7) {
                    colorIndex++;
                }
            }
            colorGenerated = true;
        }
        beginToRender();
		for (var i = 0; i < portalLength; ++i) {
            GlStateManager.pushMatrix();
            handleTextures(i);
            manipulateTextures(i);
            float[] colors = colorTable[i == 0 ? 0 : portalLength-i];
			var red = colors[0];
			var green = colors[1];
			var blue = colors[2];
            var alpha = colors[3];
            var tess = Tessellator.getInstance();
            var wr = tess.getWorldRenderer();
            wr.begin(7, DefaultVertexFormats.POSITION_COLOR);
            wr.pos(x + width, y, 0).color(red, green, blue, alpha).endVertex();
            wr.pos(x, y, 0).color(red, green, blue, alpha).endVertex();
            wr.pos(x, y + height, 0).color(red, green, blue, alpha).endVertex();
            wr.pos(x + width, y + height, 0).color(red, green, blue, alpha).endVertex();
            tess.draw();
			endTextureRendering();
		}
		endRender(false);
	}

	private void beginToRender() {
		GlStateManager.disableFog();
		GlStateManager.disableLighting();
		random.setSeed(31100L);
		GlStateManager.getFloat(2982, modelView);
		GlStateManager.getFloat(2983, projection);
	}

	private void endRender(boolean threeDimensional) {
		GlStateManager.disableBlend();
		GlStateManager.disableTexGenCoord(GlStateManager.TexGen.S);
		GlStateManager.disableTexGenCoord(GlStateManager.TexGen.T);
		GlStateManager.disableTexGenCoord(GlStateManager.TexGen.R);
		if (threeDimensional) {
			GlStateManager.enableLighting();
			GlStateManager.enableFog();
		} else Gui.drawRect(0, 0, 0, 0, 0);
	}

	private void manipulateTextures(int index) {
		GlStateManager.texGen(GlStateManager.TexGen.S, 9216);
		GlStateManager.texGen(GlStateManager.TexGen.T, 9216);
		GlStateManager.texGen(GlStateManager.TexGen.R, 9216);
		GlStateManager.texGen(GlStateManager.TexGen.S, 9474, getEndFXBuffer(1F, 0F, 0F, 0F));
		GlStateManager.texGen(GlStateManager.TexGen.T, 9474, getEndFXBuffer(0F, 1F, 0F, 0F));
		GlStateManager.texGen(GlStateManager.TexGen.R, 9474, getEndFXBuffer(0F, 0F, 1F, 0F));
		GlStateManager.enableTexGenCoord(GlStateManager.TexGen.S);
		GlStateManager.enableTexGenCoord(GlStateManager.TexGen.T);
		GlStateManager.enableTexGenCoord(GlStateManager.TexGen.R);
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(5890);
		GlStateManager.pushMatrix();
		GlStateManager.loadIdentity();
		GlStateManager.translate(0.5F, 0.5F, 0.0F);
		GlStateManager.scale(0.5F, 0.5F, 1.0F);
		float RNT = index + 1 + (Math.abs(16 - portalLength));
		GlStateManager.translate(17.0F / RNT, (2f + RNT * 1.5F) * timer.renderPartialTicks, 0.0F);
		GlStateManager.rotate((RNT * RNT * 4321.0F + RNT * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.scale(4.5F - RNT / 4.0F, 4.5F - RNT / 4.0F, 1.0F);
		GlStateManager.multMatrix(projection);
		GlStateManager.multMatrix(modelView);
	}

	private void handleTextures(int index) {
		if (index == 0) {
			mc.renderEngine.bindTexture(END_SKY_TEXTURE);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		}
		if (index >= 1)
			mc.renderEngine.bindTexture(END_PORTAL_TEXTURE);
		if (index == 1) {
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(1, 1);
		}
	}

	private void endTextureRendering() {
		GlStateManager.popMatrix();
		GlStateManager.matrixMode(5888);
		mc.renderEngine.bindTexture(END_SKY_TEXTURE);
	}

	private int getTimesToRender(double x, double y, double z) {
		var d = x * x + y * y + z * z;
		byte b;
		if (d > 36864D) b = 2;
		else if (d > 25600D) b = 4;
		else if (d > 16384D) b = 6;
		else if (d > 9216D) b = 8;
		else if (d > 4096D) b = 10;
		else if (d > 1024D) b = 12;
		else if (d > 576D) b = 14;
		else if (d > 256D) b = 15;
		else b = 16;
		return b;
	}
}