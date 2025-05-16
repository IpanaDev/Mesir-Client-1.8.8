package ipana.utils.font;

import ipana.utils.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX;
import static org.lwjgl.opengl.GL11.*;

public class TextureFont {

    private ResourceLocation texture;
    private Minecraft mc = Minecraft.getMinecraft();
    private float f,f1;
    private int textureOffsetX, textureOffsetY;
    private float fontSize;
    private CharData[] charData;
    private int offsetX,offsetY;
    private static final char colorCode = '§';
    private static final String colorIndex = "0123456789abcdefklmnor";
    private int[] colorCodes;

    public TextureFont(String fontName, int textureWidth, int textureHeight, int offsetX, int offsetY, int fontSize) {
        this.texture = new ResourceLocation("mesir/fonts/"+fontName+".png");
        this.fontSize = fontSize/100f;
        this.f = 1f / textureWidth;
        this.f1 = 1f / textureHeight;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.textureOffsetX = textureWidth/offsetX;
        this.textureOffsetY = textureHeight/offsetY;
        charData = new CharData[4096];
        colorCodes = new int[32];
        generateColors();
        long ms = System.currentTimeMillis();
        InputStream inputStream = Object.class.getResourceAsStream("/assets/minecraft/mesir/fonts/"+fontName+".png");
        if (inputStream != null) {
            try {
                BufferedImage image = ImageIO.read(inputStream);
                for (int y = 0; y < textureHeight; y+=textureOffsetY) {
                    for (int x = 0; x < textureWidth; x+=textureOffsetX) {
                        int width = 0;
                        for (int x2 = x; x2 < x+textureOffsetX; x2 += 1) {
                            for (int y2 = y; y2 < y+textureOffsetY; y2 += 1) {
                                if (x2 > image.getWidth()) {
                                    continue;
                                }
                                if (y2 > image.getHeight()) {
                                    continue;
                                }
                                int color = image.getRGB(x2,y2);
                                float alpha = (float)(color >> 24 & 255) / 255.0F;
                                if (alpha > 0.5f) {
                                    width++;
                                    break;
                                }
                            }
                        }
                        char actualChatWtf = posToChar(x,y);
                        if (actualChatWtf == 'y') {
                            System.out.println(""+width);
                        }
                        charData[actualChatWtf] = new CharData(actualChatWtf,width);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("wtf null???????");
        }
        System.out.println("Took "+(System.currentTimeMillis()-ms)+"ms to load widths");
    }

    HashMap<String, Integer> ids = new HashMap<>();

    public void drawString(String text, float x, float y, Color color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        mc.getTextureManager().bindTexture(texture);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        //glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        //GlStateManager.color(color.getRed()/255f,color.getGreen()/255f,color.getBlue()/255f);
        GL11.glColor4f(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, color.getAlpha()/255f);
        GlStateManager.translate(x,y,1);
        GlStateManager.scale(fontSize,fontSize,1);
        x=0;
        y=0;
        GL11.glBegin(7);
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            CharData data = charData[character];
            if (data != null || character == colorCode) {
                if (data != null) {
                    x += data.width + 2;
                    drawChar(character, x, y, data.width);
                } else {
                    if (i + 1 < text.length()) {
                        int index = colorIndex.indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));
                        if (index < 16) {
                            final int textColor = this.colorCodes[index];
                            GlStateManager.color((textColor >> 16) / 255.0f, (textColor >> 8 & 0xFF) / 255.0f, (textColor & 0xFF) / 255.0f, (color.getRGB() >> 24 & 0xFF) / 255.0f);
                        }
                    }
                }
            }
        }
        GL11.glEnd();
        GlStateManager.disableBlend();
        GlStateManager.bindTexture(0);
        GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
        GlStateManager.popMatrix();
    }
    private void drawChar(char character, float x, float y, int width) {
        int index = ((int) character) - 32;
        //double[] position = calcPos(index);
        int posX = index % offsetX * textureOffsetX;
        int posY = index / offsetY * textureOffsetY;
        x -= textureOffsetX/2f+width/2f;


        //worldRenderer.pos(x, y+textureOffsetY, 0).tex(posX*f, (posY+textureOffsetY)*f1).endVertex();
        glTexCoord2f(posX*f, (posY+textureOffsetY)*f1);
        glVertex2f(x, y+textureOffsetY);
        //worldRenderer.pos(x+textureOffsetX, y+textureOffsetY, 0).tex((posX+textureOffsetX)*f, (posY+textureOffsetY)*f1).endVertex();
        glTexCoord2f((posX+textureOffsetX)*f, (posY+textureOffsetY)*f1);
        glVertex2f(x+textureOffsetX, y+textureOffsetY);
        //worldRenderer.pos(x+textureOffsetX, y, 0).tex((posX+textureOffsetX)*f, posY*f1).endVertex();
        glTexCoord2f((posX+textureOffsetX)*f, posY*f1);
        glVertex2f(x+textureOffsetX, y);
        //worldRenderer.pos(x, y, 0).tex(posX*f, posY*f1).endVertex();
        glTexCoord2f(posX*f, posY*f1);
        glVertex2f(x, y);

    }

    private double[] calcPos(int index) {
        int x = index % offsetX * textureOffsetX;
        int y = index / offsetY * textureOffsetY;
        return new double[]{x,y};
    }

    private char posToChar(int x, int y) {
        int x1 = x / textureOffsetX;
        int y1 = y / textureOffsetY;
        return (char)(y1*offsetY+x1+32);
    }

    private void generateColors() {
        for (int i = 0; i < 32; ++i) {
            final int thingy = (i >> 3 & 0x1) * 85;
            int red = (i >> 2 & 0x1) * 170 + thingy;
            int green = (i >> 1 & 0x1) * 170 + thingy;
            int blue = (i & 0x1) * 170 + thingy;
            if (i == 6) {
                red += 85;
            }
            if (i >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }
            this.colorCodes[i] = ((red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF));
        }
    }

    class CharData {
        char character;
        int width;

        public CharData(char character, int width) {
            this.character = character;
            this.width = width;
        }
    }
}
