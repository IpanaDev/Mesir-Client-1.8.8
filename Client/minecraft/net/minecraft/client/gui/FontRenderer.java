
package net.minecraft.client.gui;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import optifine.Config;
import optifine.CustomColors;
import optifine.FontUtils;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class FontRenderer implements IResourceManagerReloadListener {
    private static final ResourceLocation[] unicodePageLocations = new ResourceLocation[256];
    private float[] charWidth = new float[256];
    public int FONT_HEIGHT = 9;
    public Random fontRandom = new Random();
    private byte[] glyphWidth = new byte[65536];
    public int[] colorCode = new int[32];
    private ResourceLocation locationFontTexture;
    private final TextureManager renderEngine;
    private float posX;
    private float posY;
    private boolean unicodeFlag;
    private boolean bidiFlag;
    private float red;
    private float blue;
    private float green;
    private float alpha;
    private int textColor;
    private boolean randomStyle;
    private boolean boldStyle;
    private boolean italicStyle;
    private boolean underlineStyle;
    private boolean strikethroughStyle;
    public GameSettings gameSettings;
    public ResourceLocation locationFontTextureBase;
    public boolean enabled = true;
    public float offsetBold = 1.0F;
    private DrawMode drawMode;
    private boolean autoBegin;
    private int lastUnicodePage;

    public FontRenderer(GameSettings gameSettingsIn, ResourceLocation location, TextureManager textureManagerIn, boolean unicode) {
        this.drawMode = FontRenderer.DrawMode.NONE;
        this.autoBegin = true;
        this.lastUnicodePage = -1;
        this.gameSettings = gameSettingsIn;
        this.locationFontTextureBase = location;
        this.locationFontTexture = location;
        this.renderEngine = textureManagerIn;
        this.unicodeFlag = unicode;
        this.locationFontTexture = FontUtils.getHdFontLocation(this.locationFontTextureBase);
        this.renderEngine.bindTexture(this.locationFontTexture);

        for(int i = 0; i < 32; ++i) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;
            if (i == 6) {
                k += 85;
            }

            if (gameSettingsIn.anaglyph) {
                int j1 = (k * 30 + l * 59 + i1 * 11) / 100;
                int k1 = (k * 30 + l * 70) / 100;
                int l1 = (k * 30 + i1 * 70) / 100;
                k = j1;
                l = k1;
                i1 = l1;
            }

            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }

            this.colorCode[i] = (k & 255) << 16 | (l & 255) << 8 | i1 & 255;
        }

        this.readGlyphSizes();
    }

    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.locationFontTexture = FontUtils.getHdFontLocation(this.locationFontTextureBase);
        Arrays.fill(unicodePageLocations, null);
        this.readFontTexture();
        this.readGlyphSizes();
    }

    private void readFontTexture() {
        BufferedImage bufferedimage;
        try {
            bufferedimage = TextureUtil.readBufferedImage(this.getResourceInputStream(this.locationFontTexture));
        } catch (IOException var21) {
            throw new RuntimeException(var21);
        }

        Properties properties = FontUtils.readFontProperties(this.locationFontTexture);
        int i = bufferedimage.getWidth();
        int j = bufferedimage.getHeight();
        int k = i / 16;
        int l = j / 16;
        float f = (float)i / 128.0F;
        float f1 = Config.limit(f, 1.0F, 2.0F);
        this.offsetBold = 1.0F / f1;
        float f2 = FontUtils.readFloat(properties, "offsetBold", -1.0F);
        if (f2 >= 0.0F) {
            this.offsetBold = f2;
        }

        int[] aint = new int[i * j];
        bufferedimage.getRGB(0, 0, i, j, aint, 0, i);

        for(int i1 = 0; i1 < 256; ++i1) {
            int j1 = i1 % 16;
            int k1 = i1 / 16;

            int l1;
            for(l1 = k - 1; l1 >= 0; --l1) {
                int i2 = j1 * k + l1;
                boolean flag = true;

                for(int j2 = 0; j2 < l; ++j2) {
                    int k2 = (k1 * l + j2) * i;
                    int l2 = aint[i2 + k2];
                    int i3 = l2 >> 24 & 255;
                    if (i3 > 16) {
                        flag = false;
                        break;
                    }
                }

                if (!flag) {
                    break;
                }
            }

            if (i1 == 32) {
                if (k <= 8) {
                    l1 = (int)(2.0F * f);
                } else {
                    l1 = (int)(1.5F * f);
                }
            }

            this.charWidth[i1] = (float)(l1 + 1) / f + 1.0F;
        }

        FontUtils.readCustomCharWidths(properties, this.charWidth);
    }

    private void readGlyphSizes() {
        InputStream inputstream = null;

        try {
            inputstream = this.getResourceInputStream(new ResourceLocation("font/glyph_sizes.bin"));
            inputstream.read(this.glyphWidth);
        } catch (IOException var6) {
            throw new RuntimeException(var6);
        } finally {
            IOUtils.closeQuietly(inputstream);
        }

    }

    private float func_181559_a(char p_181559_1_, boolean p_181559_2_, boolean p_78255_2_, int color) {
        if (p_181559_1_ == ' ') {
            return !this.unicodeFlag ? this.charWidth[p_181559_1_] : 4.0F;
        } else {
            int i = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(p_181559_1_);
            return i != -1 && !this.unicodeFlag ? this.renderDefaultChar(i, p_181559_2_, p_78255_2_, color) : this.renderUnicodeChar(p_181559_1_, p_181559_2_, p_78255_2_, color);
        }
    }

    private ResourceLocation getUnicodePageLocation(int p_111271_1_) {
        if (unicodePageLocations[p_111271_1_] == null) {
            unicodePageLocations[p_111271_1_] = new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", p_111271_1_));
            unicodePageLocations[p_111271_1_] = FontUtils.getHdFontLocation(unicodePageLocations[p_111271_1_]);
        }

        return unicodePageLocations[p_111271_1_];
    }

    private void loadGlyphTexture(int p_78257_1_) {
        this.bindTexture(this.getUnicodePageLocation(p_78257_1_));
    }

    private float renderDefaultChar(int p_78266_1_, boolean p_78266_2_, boolean p_78255_2_, int c) {
        int i = p_78266_1_ % 16 * 8;
        int j = p_78266_1_ / 16 * 8;
        int k = p_78266_2_ ? 1 : 0;
        float f = this.charWidth[p_78266_1_];
        float f1 = 7.99F;
        int color = p_78255_2_ ? (c & 16579836) >> 2 | c & -16777216 : c;
        int r = color >> 16 & 255;
        int g = color >> 8 & 255;
        int b = color & 255;
        int a = (int)(this.alpha * 255.0F);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        if (this.drawMode != FontRenderer.DrawMode.NORMAL) {
            if (this.drawMode == FontRenderer.DrawMode.UNICODE) {
                tessellator.draw();
                this.bindTexture(this.locationFontTexture);
                worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            } else {
                this.bindTexture(this.locationFontTexture);
            }

            this.drawMode = FontRenderer.DrawMode.NORMAL;
        }

        if (p_78255_2_) {
            float pX = this.posX;
            float pY = this.posY;
            this.posX += 0.5F;
            this.posY += 0.5F;
            float zLevel = -1.0E-4F;
            worldRenderer.pos(this.posX - (float)k, this.posY + f1, zLevel).tex((float)i / 128.0F, ((float)j + f1) / 128.0F).color(r, g, b, a).endVertex();
            worldRenderer.pos(this.posX - (float)k + f1 - 1.0F, this.posY + f1, zLevel).tex(((float)i + f1 - 1.0F) / 128.0F, ((float)j + f1) / 128.0F).color(r, g, b, a).endVertex();
            worldRenderer.pos(this.posX + f1 - 1.0F + (float)k, this.posY, zLevel).tex(((float)i + f1 - 1.0F) / 128.0F, (float)j / 128.0F).color(r, g, b, a).endVertex();
            worldRenderer.pos(this.posX + (float)k, this.posY, zLevel).tex((float)i / 128.0F, (float)j / 128.0F).color(r, g, b, a).endVertex();
            r = c >> 16 & 255;
            g = c >> 8 & 255;
            b = c & 255;
            this.posX = pX;
            this.posY = pY;
        }

        worldRenderer.pos(this.posX - (float)k, this.posY + f1, 0.0F).tex((float)i / 128.0F, ((float)j + f1) / 128.0F).color(r, g, b, a).endVertex();
        worldRenderer.pos(this.posX - (float)k + f1 - 1.0F, this.posY + f1, 0.0F).tex(((float)i + f1 - 1.0F) / 128.0F, ((float)j + f1) / 128.0F).color(r, g, b, a).endVertex();
        worldRenderer.pos(this.posX + f1 - 1.0F + (float)k, this.posY, 0.0F).tex(((float)i + f1 - 1.0F) / 128.0F, (float)j / 128.0F).color(r, g, b, a).endVertex();
        worldRenderer.pos(this.posX + (float)k, this.posY, 0.0F).tex((float)i / 128.0F, (float)j / 128.0F).color(r, g, b, a).endVertex();
        return f;
    }

    private float renderUnicodeChar(char p_78277_1_, boolean p_78277_2_, boolean p_78255_2_, int color) {
        if (this.glyphWidth[p_78277_1_] == 0) {
            return 0.0F;
        } else {
            int c = p_78255_2_ ? (color & 16579836) >> 2 | color & -16777216 : color;
            int r = c >> 16 & 255;
            int g = c >> 8 & 255;
            int b = c & 255;
            int a = (int)(this.alpha * 255.0F);
            int i = p_78277_1_ / 256;
            int j = this.glyphWidth[p_78277_1_] >>> 4;
            int k = this.glyphWidth[p_78277_1_] & 15;
            j &= 15;
            float f = (float)j;
            float f1 = (float)(k + 1);
            float f2 = (float)(p_78277_1_ % 16 * 16) + f;
            float f3 = (float)((p_78277_1_ & 255) / 16 * 16);
            float f4 = f1 - f - 0.02F;
            float f5 = p_78277_2_ ? 1.0F : 0.0F;
            float offset = 7.99F;
            Tessellator tessellator = Tessellator.getInstance();
            WorldRenderer worldRenderer = tessellator.getWorldRenderer();
            if (this.lastUnicodePage == -1) {
                this.lastUnicodePage = i;
            }

            if (this.drawMode != FontRenderer.DrawMode.UNICODE) {
                if (this.drawMode == FontRenderer.DrawMode.NORMAL) {
                    tessellator.draw();
                    this.loadGlyphTexture(i);
                    worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                } else {
                    this.loadGlyphTexture(i);
                }

                this.lastUnicodePage = i;
                this.drawMode = FontRenderer.DrawMode.UNICODE;
            } else if (this.lastUnicodePage != i) {
                tessellator.draw();
                this.loadGlyphTexture(i);
                worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                this.lastUnicodePage = i;
            }

            if (p_78255_2_) {
                float pX = this.posX;
                float pY = this.posY;
                this.posX += 0.5F;
                this.posY += 0.5F;
                worldRenderer.pos(this.posX - f5, this.posY + offset, 0.0F).tex(f2 / 256.0F, (f3 + offset * 2.0F) / 256.0F).color(r, g, b, a).endVertex();
                worldRenderer.pos(this.posX + f4 / 2.0F - f5, this.posY + offset, 0.0F).tex((f2 + f4) / 256.0F, (f3 + offset * 2.0F) / 256.0F).color(r, g, b, a).endVertex();
                worldRenderer.pos(this.posX + f4 / 2.0F + f5, this.posY, 0.0F).tex((f2 + f4) / 256.0F, f3 / 256.0F).color(r, g, b, a).endVertex();
                worldRenderer.pos(this.posX + f5, this.posY, 0.0F).tex(f2 / 256.0F, f3 / 256.0F).color(r, g, b, a).endVertex();
                r = color >> 16 & 255;
                g = color >> 8 & 255;
                b = color & 255;
                this.posX = pX;
                this.posY = pY;
            }

            worldRenderer.pos(this.posX - f5, this.posY + offset, 0.0F).tex(f2 / 256.0F, (f3 + offset * 2.0F) / 256.0F).color(r, g, b, a).endVertex();
            worldRenderer.pos(this.posX + f4 / 2.0F - f5, this.posY + offset, 0.0F).tex((f2 + f4) / 256.0F, (f3 + offset * 2.0F) / 256.0F).color(r, g, b, a).endVertex();
            worldRenderer.pos(this.posX + f4 / 2.0F + f5, this.posY, 0.0F).tex((f2 + f4) / 256.0F, f3 / 256.0F).color(r, g, b, a).endVertex();
            worldRenderer.pos(this.posX + f5, this.posY, 0.0F).tex(f2 / 256.0F, f3 / 256.0F).color(r, g, b, a).endVertex();
            return (f1 - f) / 2.0F + 1.0F;
        }
    }

    public int drawStringWithShadow(String text, float x, float y, int color) {
        return this.drawString(text, x, y, color, true);
    }

    public void drawBorderedString(String text, float x, float y, int color, float width) {
        this.drawString(StringUtils.stripControlCodes(text), x + width, y + width, Color.black.getRGB(), false);
        this.drawString(StringUtils.stripControlCodes(text), x - width, y - width, Color.black.getRGB(), false);
        this.drawString(StringUtils.stripControlCodes(text), x + width, y - width, Color.black.getRGB(), false);
        this.drawString(StringUtils.stripControlCodes(text), x - width, y + width, Color.black.getRGB(), false);
        this.drawString(StringUtils.stripControlCodes(text), x + width, y, Color.black.getRGB(), false);
        this.drawString(StringUtils.stripControlCodes(text), x, y + width, Color.black.getRGB(), false);
        this.drawString(StringUtils.stripControlCodes(text), x - width, y, Color.black.getRGB(), false);
        this.drawString(StringUtils.stripControlCodes(text), x, y - width, Color.black.getRGB(), false);
        this.drawString(text, x, y, color, false);
    }

    public int drawString(String text, double x, double y, int color) {
        return !this.enabled ? 0 : this.drawString(text, (float)x, (float)y, color, false);
    }

    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        this.enableAlpha();
        this.resetStyles();
        return this.renderString(text, x, y, color, dropShadow);
    }

    private String bidiReorder(String p_147647_1_) {
        try {
            Bidi bidi = new Bidi((new ArabicShaping(8)).shape(p_147647_1_), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        } catch (ArabicShapingException var3) {
            return p_147647_1_;
        }
    }

    private void resetStyles() {
        this.randomStyle = false;
        this.boldStyle = false;
        this.italicStyle = false;
        this.underlineStyle = false;
        this.strikethroughStyle = false;
    }

    public void bindFontTexture() {
        this.bindTexture(this.locationFontTexture);
    }

    private void renderStringAtPos(String p_78255_1_, boolean p_78255_2_, int color) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        if (this.autoBegin) {
            this.begin();
        }

        for(int i = 0; i < p_78255_1_.length(); ++i) {
            char c0 = p_78255_1_.charAt(i);
            int i1;
            int j1;
            if (c0 == 167 && i + 1 < p_78255_1_.length()) {
                i1 = "0123456789abcdefklmnor".indexOf(p_78255_1_.toLowerCase().charAt(i + 1));
                if (i1 < 16) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    if (i1 < 0) {
                        i1 = 15;
                    }

                    j1 = this.colorCode[i1];
                    if (Config.isCustomColors()) {
                        j1 = CustomColors.getTextColor(i1, j1);
                    }

                    this.textColor = j1;
                    color = j1;
                    this.setColor((float)(j1 >> 16 & 255) / 255.0F, (float)(j1 >> 8 & 255) / 255.0F, (float)(j1 & 255) / 255.0F, this.alpha);
                } else if (i1 == 16) {
                    this.randomStyle = true;
                } else if (i1 == 17) {
                    this.boldStyle = true;
                } else if (i1 == 18) {
                    this.strikethroughStyle = true;
                } else if (i1 == 19) {
                    this.underlineStyle = true;
                } else if (i1 == 20) {
                    this.italicStyle = true;
                } else {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    color = (new Color((int)(this.red * 255.0F), (int)(this.green * 255.0F), (int)(this.blue * 255.0F), 255)).getRGB();
                    this.setColor(this.red, this.green, this.blue, 1.0F);
                }

                ++i;
            } else {
                i1 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(c0);
                if (this.randomStyle && i1 != -1) {
                    j1 = this.getCharWidth(c0);

                    char c1;
                    do {
                        i1 = this.fontRandom.nextInt("ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".length());
                        c1 = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".charAt(i1);
                    } while(j1 != this.getCharWidth(c1));

                    c0 = c1;
                }

                float f1 = i1 != -1 && !this.unicodeFlag ? this.offsetBold : 0.5F;
                boolean flag = (c0 == 0 || i1 == -1 || this.unicodeFlag) && p_78255_2_;
                if (flag) {
                    this.posX -= f1;
                    this.posY -= f1;
                }

                float f = this.func_181559_a(c0, this.italicStyle, p_78255_2_, color);
                if (flag) {
                    this.posX += f1;
                    this.posY += f1;
                }

                if (this.boldStyle) {
                    this.posX += f1;
                    if (flag) {
                        this.posX -= f1;
                        this.posY -= f1;
                    }

                    this.func_181559_a(c0, this.italicStyle, p_78255_2_, color);
                    this.posX -= f1;
                    if (flag) {
                        this.posX += f1;
                        this.posY += f1;
                    }

                    f += f1;
                }

                if (this.strikethroughStyle) {
                    tessellator.draw();
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    worldRenderer.begin(7, DefaultVertexFormats.POSITION);
                    worldRenderer.pos(this.posX, (this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0).endVertex();
                    worldRenderer.pos((this.posX + f), (this.posY + (float)(this.FONT_HEIGHT / 2)), 0.0).endVertex();
                    worldRenderer.pos((this.posX + f), (this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0).endVertex();
                    worldRenderer.pos(this.posX, (this.posY + (float)(this.FONT_HEIGHT / 2) - 1.0F), 0.0).endVertex();
                    tessellator.draw();
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                    worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                }

                if (this.underlineStyle) {
                    tessellator.draw();
                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                    worldRenderer.begin(7, DefaultVertexFormats.POSITION);
                    int l = this.underlineStyle ? -1 : 0;
                    worldRenderer.pos((this.posX + (float)l), (this.posY + (float)this.FONT_HEIGHT), 0.0).endVertex();
                    worldRenderer.pos((this.posX + f), (this.posY + (float)this.FONT_HEIGHT), 0.0).endVertex();
                    worldRenderer.pos((this.posX + f), (this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0).endVertex();
                    worldRenderer.pos((this.posX + (float)l), (this.posY + (float)this.FONT_HEIGHT - 1.0F), 0.0).endVertex();
                    tessellator.draw();
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                    worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                }

                this.posX += f;
            }
        }

        if (this.autoBegin) {
            this.end();
        }

        this.drawMode = FontRenderer.DrawMode.NONE;
        this.lastUnicodePage = -1;
    }

    private void renderStringAligned(String text, int x, int y, int p_78274_4_, int color) {
        if (this.bidiFlag) {
            int i = this.getStringWidth(this.bidiReorder(text));
            x = x + p_78274_4_ - i;
        }

        this.renderString(text, (float)x, (float)y, color, false);
    }

    private int renderString(String text, float x, float y, int color, boolean dropShadow) {
        if (text == null) {
            return 0;
        } else {
            if (this.bidiFlag) {
                text = this.bidiReorder(text);
            }

            if ((color & -67108864) == 0) {
                color |= -16777216;
            }

            this.red = (float)(color >> 16 & 255) / 255.0F;
            this.blue = (float)(color >> 8 & 255) / 255.0F;
            this.green = (float)(color & 255) / 255.0F;
            this.alpha = (float)(color >> 24 & 255) / 255.0F;
            this.setColor(this.red, this.blue, this.green, this.alpha);
            this.posX = x;
            this.posY = y;
            this.renderStringAtPos(text, dropShadow, color);
            return (int)this.posX;
        }
    }

    public int getStringWidth(String text) {
        if (text == null) {
            return 0;
        } else {
            float f = 0.0F;
            boolean flag = false;

            for(int i = 0; i < text.length(); ++i) {
                char c0 = text.charAt(i);
                float f1 = this.getCharWidthFloat(c0);
                if (f1 < 0.0F && i < text.length() - 1) {
                    ++i;
                    c0 = text.charAt(i);
                    if (c0 != 'l' && c0 != 'L') {
                        if (c0 == 'r' || c0 == 'R') {
                            flag = false;
                        }
                    } else {
                        flag = true;
                    }

                    f1 = 0.0F;
                }

                f += f1;
                if (flag && f1 > 0.0F) {
                    f += this.unicodeFlag ? 1.0F : this.offsetBold;
                }
            }

            return (int)f;
        }
    }

    public int getCharWidth(char character) {
        return Math.round(this.getCharWidthFloat(character));
    }

    private float getCharWidthFloat(char p_getCharWidthFloat_1_) {
        if (p_getCharWidthFloat_1_ == 167) {
            return -1.0F;
        } else if (p_getCharWidthFloat_1_ == ' ') {
            return this.charWidth[32];
        } else {
            int i = "ÀÁÂÈÊËÍÓÔÕÚßãõğİıŒœŞşŴŵžȇ\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000ÇüéâäàåçêëèïîìÄÅÉæÆôöòûùÿÖÜø£Ø×ƒáíóúñÑªº¿®¬½¼¡«»░▒▓│┤╡╢╖╕╣║╗╝╜╛┐└┴┬├─┼╞╟╚╔╩╦╠═╬╧╨╤╥╙╘╒╓╫╪┘┌█▄▌▐▀αβΓπΣσμτΦΘΩδ∞∅∈∩≡±≥≤⌠⌡÷≈°∙·√ⁿ²■\u0000".indexOf(p_getCharWidthFloat_1_);
            if (p_getCharWidthFloat_1_ > 0 && i != -1 && !this.unicodeFlag) {
                return this.charWidth[i];
            } else if (this.glyphWidth[p_getCharWidthFloat_1_] != 0) {
                int j = this.glyphWidth[p_getCharWidthFloat_1_] >>> 4;
                int k = this.glyphWidth[p_getCharWidthFloat_1_] & 15;
                j &= 15;
                ++k;
                return (float)((k - j) / 2 + 1);
            } else {
                return 0.0F;
            }
        }
    }

    public String trimStringToWidth(String text, int width) {
        return this.trimStringToWidth(text, width, false);
    }

    public String trimStringToWidth(String text, int width, boolean reverse) {
        StringBuilder stringbuilder = new StringBuilder();
        float f = 0.0F;
        int i = reverse ? text.length() - 1 : 0;
        int j = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for(int k = i; k >= 0 && k < text.length() && f < (float)width; k += j) {
            char c0 = text.charAt(k);
            float f1 = this.getCharWidthFloat(c0);
            if (flag) {
                flag = false;
                if (c0 != 'l' && c0 != 'L') {
                    if (c0 == 'r' || c0 == 'R') {
                        flag1 = false;
                    }
                } else {
                    flag1 = true;
                }
            } else if (f1 < 0.0F) {
                flag = true;
            } else {
                f += f1;
                if (flag1) {
                    ++f;
                }
            }

            if (f > (float)width) {
                break;
            }

            if (reverse) {
                stringbuilder.insert(0, c0);
            } else {
                stringbuilder.append(c0);
            }
        }

        return stringbuilder.toString();
    }

    private String trimStringNewline(String text) {
        while(text != null && text.endsWith("\n")) {
            text = text.substring(0, text.length() - 1);
        }

        return text;
    }

    public void drawSplitString(String str, int x, int y, int wrapWidth, int textColor) {
        this.resetStyles();
        this.textColor = textColor;
        str = this.trimStringNewline(str);
        this.renderSplitString(str, x, y, wrapWidth);
    }

    private void renderSplitString(String str, int x, int y, int wrapWidth) {
        for (String s : this.listFormattedStringToWidth(str, wrapWidth)) {
            this.renderStringAligned(s, x, y, wrapWidth, this.textColor);
            y += this.FONT_HEIGHT;
        }

    }

    public int splitStringWidth(String p_78267_1_, int p_78267_2_) {
        return this.FONT_HEIGHT * this.listFormattedStringToWidth(p_78267_1_, p_78267_2_).size();
    }

    public void setUnicodeFlag(boolean unicodeFlagIn) {
        this.unicodeFlag = unicodeFlagIn;
    }

    public boolean getUnicodeFlag() {
        return this.unicodeFlag;
    }

    public void setBidiFlag(boolean bidiFlagIn) {
        this.bidiFlag = bidiFlagIn;
    }

    public List<String> listFormattedStringToWidth(String str, int wrapWidth) {
        return Arrays.asList(this.wrapFormattedStringToWidth(str, wrapWidth).split("\n"));
    }

    String wrapFormattedStringToWidth(String str, int wrapWidth) {
        int i = this.sizeStringToWidth(str, wrapWidth);
        if (str.length() <= i) {
            return str;
        } else {
            String s = str.substring(0, i);
            char c0 = str.charAt(i);
            boolean flag = c0 == ' ' || c0 == '\n';
            String var10000 = getFormatFromString(s);
            String s1 = var10000 + str.substring(i + (flag ? 1 : 0));
            return s + "\n" + this.wrapFormattedStringToWidth(s1, wrapWidth);
        }
    }

    private int sizeStringToWidth(String str, int wrapWidth) {
        int i = str.length();
        float f = 0.0F;
        int j = 0;
        int k = -1;

        for(boolean flag = false; j < i; ++j) {
            char c0 = str.charAt(j);
            switch (c0) {
                case '\n':
                    --j;
                    break;
                case ' ':
                    k = j;
                default:
                    f += this.getCharWidthFloat(c0);
                    if (flag) {
                        ++f;
                    }
                    break;
                case '§':
                    if (j < i - 1) {
                        ++j;
                        char c1 = str.charAt(j);
                        if (c1 != 'l' && c1 != 'L') {
                            if (c1 == 'r' || c1 == 'R' || isFormatColor(c1)) {
                                flag = false;
                            }
                        } else {
                            flag = true;
                        }
                    }
            }

            if (c0 == '\n') {
                ++j;
                k = j;
                break;
            }

            if (f > (float)wrapWidth) {
                break;
            }
        }

        return j != i && k != -1 && k < j ? k : j;
    }

    private static boolean isFormatColor(char colorChar) {
        return colorChar >= '0' && colorChar <= '9' || colorChar >= 'a' && colorChar <= 'f' || colorChar >= 'A' && colorChar <= 'F';
    }

    private static boolean isFormatSpecial(char formatChar) {
        return formatChar >= 'k' && formatChar <= 'o' || formatChar >= 'K' && formatChar <= 'O' || formatChar == 'r' || formatChar == 'R';
    }

    public static String getFormatFromString(String text) {
        StringBuilder s = new StringBuilder();
        int i = -1;
        int j = text.length();

        while((i = text.indexOf(167, i + 1)) != -1) {
            if (i < j - 1) {
                char c0 = text.charAt(i + 1);
                if (isFormatColor(c0)) {
                    s = new StringBuilder("§" + c0);
                } else if (isFormatSpecial(c0)) {
                    s.append("§").append(c0);
                }
            }
        }

        return s.toString();
    }

    public boolean getBidiFlag() {
        return this.bidiFlag;
    }

    public int getColorCode(char character) {
        int i = "0123456789abcdef".indexOf(character);
        if (i >= 0 && i < this.colorCode.length) {
            int j = this.colorCode[i];
            if (Config.isCustomColors()) {
                j = CustomColors.getTextColor(i, j);
            }

            return j;
        } else {
            return 16777215;
        }
    }

    protected void setColor(float p_setColor_1_, float p_setColor_2_, float p_setColor_3_, float p_setColor_4_) {
        GlStateManager.color(p_setColor_1_, p_setColor_2_, p_setColor_3_, p_setColor_4_);
    }

    protected void enableAlpha() {
        GlStateManager.enableAlpha();
    }

    protected void bindTexture(ResourceLocation p_bindTexture_1_) {
        this.renderEngine.bindTexture(p_bindTexture_1_, true);
    }

    protected InputStream getResourceInputStream(ResourceLocation p_getResourceInputStream_1_) throws IOException {
        return Minecraft.getMinecraft().getResourceManager().getResource(p_getResourceInputStream_1_).getInputStream();
    }

    public void begin() {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
    }

    public void end() {
        Tessellator.getInstance().draw();
    }

    public boolean autoBegin() {
        return this.autoBegin;
    }

    public void setAutoBegin(boolean _autoBegin) {
        this.autoBegin = _autoBegin;
    }

    public enum DrawMode {
        NONE,
        NORMAL,
        UNICODE

    }
}
