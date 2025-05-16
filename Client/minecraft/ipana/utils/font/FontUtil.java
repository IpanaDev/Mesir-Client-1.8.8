package ipana.utils.font;

import ipana.utils.shader.ShaderManager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StringUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.Locale;

import static org.lwjgl.opengl.GL11.*;

public class FontUtil {
    private Font font;
    private boolean fractionalMetrics;
    public CharacterData[] characterData;
    private int[] colorCodes;
    private static int RANDOM_OFFSET;
    public static final char colorCode = '§';
    public static final String colorIndex = "0123456789abcdefklmnor";
    private long ms = -1;
    private float texCoordX;
    private float texCoordY;
    private int textureId;
    private boolean autoBegin = true;
    private int atlas;
    public float MAX_HEIGHT;

    static {
        FontUtil.RANDOM_OFFSET = 1;
    }


    public FontUtil(final Font font, final boolean disableAA, Style style) {
        this(font, 512, disableAA, style);
    }

    public FontUtil(final Font font, final int characterCount, final boolean disableAA, Style style) {
        this(font, characterCount, true, disableAA, style);
    }

    public FontUtil(final Font font, final int characterCount, final boolean fractionalMetrics, final boolean disableAA, Style style) {
        long ms = System.currentTimeMillis();
        this.colorCodes = new int[32];
        this.font = font;
        this.fractionalMetrics = fractionalMetrics;

        switch (style) { //
            case REGULAR -> this.characterData = this.setup(new CharacterData[characterCount], 0, disableAA);
            case BOLD -> this.characterData = this.setup(new CharacterData[characterCount], 1, disableAA);
            case ITALIC -> this.characterData = this.setup(new CharacterData[characterCount], 2, disableAA);
        }

        System.out.println("A Font created in: " + (System.currentTimeMillis() - ms) + "ms (Size: "+font.getSize()+", Resolution: "+atlas+")");
    }

    private CharacterData[] setup(final CharacterData[] characterData, final int type, final boolean disableAA) {
        this.generateColors();
        final Font font = this.font.deriveFont(type);
        final BufferedImage utilityImage = new BufferedImage(1, 1, 2);
        final Graphics2D utilityGraphics = (Graphics2D) utilityImage.getGraphics();
        utilityGraphics.setFont(font);
        final FontMetrics fontMetrics = utilityGraphics.getFontMetrics();
        float[] maxSizes = getMaxSizes(characterData);
        int size = MathHelper.ceiling_double_int(Math.sqrt(maxSizes[0]*maxSizes[1]));
        this.textureId = GL11.glGenTextures();
        this.atlas = size;
        texCoordX = 1f / size;
        texCoordY = 1f / size;
        final ByteBuffer buffer = BufferUtils.createByteBuffer(size * size * 4);
        final BufferedImage characterImage = new BufferedImage(size, size, 2);
        float lastX = 4;
        float lastY = fontMetrics.getAscent();
        for (int index = 0; index < characterData.length; index++) {
            final char character = (char) index;
            final Rectangle2D characterBounds = fontMetrics.getStringBounds(String.valueOf(character), utilityGraphics);
            float x = lastX;
            float y = lastY;
            float width = (float) (characterBounds.getWidth()+8);
            float height = (float) characterBounds.getHeight();

            final Graphics2D graphics = (Graphics2D) characterImage.getGraphics();
            graphics.setFont(font);
            graphics.setColor(Color.WHITE);
            if (disableAA) {
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, this.fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            }
            graphics.drawString(String.valueOf(character), x, y);
            characterData[index] = new CharacterData(character, x, y-fontMetrics.getAscent(), width, height);
            if (MAX_HEIGHT < height) {
                MAX_HEIGHT = height;
            }
            lastX+=width+16;
            if (lastX+width > size) {
                lastX = 4;
                lastY += maxSizes[1]-fontMetrics.getAscent();
                if (lastY > size) {
                    System.out.println("Can't keep building font because its out of bounds. " + index);
                    break;
                }
            }
        }
        putTexture(size, size, characterImage, buffer);
        buffer.flip();
        GlStateManager.bindTexture(textureId);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size, size, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        return characterData;
    }

    private float[] getMaxSizes(CharacterData[] characterData) {
        final BufferedImage utilityImage = new BufferedImage(1, 1, 2);
        final Graphics2D utilityGraphics = (Graphics2D) utilityImage.getGraphics();
        utilityGraphics.setFont(font);
        final FontMetrics fontMetrics = utilityGraphics.getFontMetrics();
        float maxWidth = 0;
        float maxHeight = fontMetrics.getAscent();
        for (int index = 0; index < characterData.length; ++index) {
            final char character = (char) index;
            final Rectangle2D characterBounds = fontMetrics.getStringBounds(String.valueOf(character), utilityGraphics);
            maxWidth+=characterBounds.getWidth()+16;
            float height = (float) (characterBounds.getHeight()+fontMetrics.getAscent()+4);
            if (maxHeight < height) {
                maxHeight = height;
            }
        }
        return new float[] {maxWidth, maxHeight};
    }


    private void putTexture(int width, int height, final BufferedImage image, ByteBuffer buffer) {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                final int pixel = image.getRGB(x,y);
                int r = (pixel >> 16 & 0xFF);
                int g = (pixel >> 8 & 0xFF);
                int b = (pixel & 0xFF);
                int a = (pixel >> 24 & 0xFF);
                buffer.put((byte) r);
                buffer.put((byte) g);
                buffer.put((byte) b);
                buffer.put((byte) a);
            }
        }
    }

    public void drawString(final String text, final float x, final float y, final int color) {
        this.drawString(text, x, y, new Color(color));
    }

    public void drawString(final String text, final float x, final float y, final Color color) {
        this.renderString(text, (int)x, (int)y, color, false);
    }

    public void drawCenteredString(final String text, final float x, final float y, final int color) {
        this.drawCenteredString(text, x, y, new Color(color));
    }

    public void drawCenteredString(final String text, final float x, final float y, final Color color) {
        final float width = this.getWidth(text) / 2.0f;
        final float height = this.getHeight(text) / 2.0f;
        this.renderString(text, x - width, y - height, color, false);
    }

    public void drawBorderedString(final String text, final float x, final float y, final int inColor, int out, float width) {
        drawBorderedString(text, x, y, new Color(inColor), new Color(out), width);
    }

    public void drawBorderedString(final String text, final float x, final float y, final Color inColor, Color outColor, float width) {
        String stripped = StringUtils.stripControlCodes(text);
        this.renderString(stripped, x - width, y, outColor, false);
        this.renderString(stripped, x + width, y, outColor, false);
        this.renderString(stripped, x, y - width, outColor, false);
        this.renderString(stripped, x, y + width, outColor, false);
        this.renderString(text, x, y, inColor, false);
    }

    public void drawBorderedString(final String text, final float x, final float y, final Color inColor, float width) {
        ShaderManager shaderManager = ShaderManager.getInstance();
        if (ms == -1) {
            ms = System.currentTimeMillis();
        }
        shaderManager.loadShader("make_gold");
        shaderManager.loadData("make_gold", "amount", (ms - System.currentTimeMillis() - 1) / 2000f);
        shaderManager.loadData("make_gold", "offset", 5f);
        String stripped = StringUtils.stripControlCodes(text);
        this.renderString(stripped, x - width, y, Color.white, false);
        this.renderString(stripped, x + width, y, Color.white, false);
        this.renderString(stripped, x, y - width, Color.white, false);
        this.renderString(stripped, x, y + width, Color.black, false);
        shaderManager.stop("make_gold");
        this.renderString(text, x, y, inColor, false);
    }

    private void renderString(final String text, float x, float y, final Color color, boolean dropShadow) {
        if (text.length() != 0) {
            if (autoBegin) {
                begin();
            }
            x -= 2.0f;
            y -= 2.0f;
            x += 0.5f;
            y += 0.5f;
            x *= 2.0f;
            y *= 2.0f;
            CharacterData[] characterData = this.characterData;
            boolean underlined = false;
            boolean strikethrough = false;
            boolean obfuscated = false;
            final int length = text.length();
            final float multiplier = 255.0f;
            Color c = color;
            GlStateManager.color(c.getRed() / multiplier, c.getGreen() / multiplier, c.getBlue() / multiplier, (float) c.getAlpha());
            for (int i = 0; i < length; ++i) {
                char character = text.charAt(i);
                final char previous = (i > 0) ? text.charAt(i - 1) : '.';
                if (previous != colorCode) {
                    if (character == colorCode) {
                        int index = colorIndex.indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));
                        if (index >= 16) {
                            if (index == 16) {
                                obfuscated = true;
                            } else if (index == 17) {
                                characterData = this.characterData;
                            } else if (index == 18) {
                                strikethrough = true;
                            } else if (index == 19) {
                                underlined = true;
                            } else if (index == 20) {
                                characterData = this.characterData;
                            } else {
                                obfuscated = false;
                                strikethrough = false;
                                underlined = false;
                                characterData = this.characterData;
                                GlStateManager.color((1.0f), (1.0f), (1.0f), color.getAlpha() / 255.0f);
                                c = new Color(255, 255, 255, color.getAlpha());
                            }
                        } else {
                            obfuscated = false;
                            strikethrough = false;
                            underlined = false;
                            characterData = this.characterData;
                            if (index < 0) {
                                index = 15;
                            }
                            final int textColor = this.colorCodes[index];
                            float r = (textColor >> 16) / 255.0f;
                            float g = (textColor >> 8 & 0xFF) / 255.0f;
                            float b = (textColor & 0xFF) / 255.0f;
                            GlStateManager.color(r, g, b, color.getAlpha() / 255.0f);
                            c = new Color((int) (r * 255), (int) (g * 255), (int) (b * 255));
                        }
                    } else {
                        if (obfuscated) {
                            character += (char) FontUtil.RANDOM_OFFSET;
                        }
                        this.drawChar(character, characterData, x, y, c, dropShadow);
                        if (character >= characterData.length) {
                            continue;
                        }
                        final CharacterData charData = characterData[character];
                        if (strikethrough) {
                            this.drawLine(new Vector2d(0.0, charData.height / 2.0), new Vector2d(charData.width, charData.height / 2.0), 3.0f);
                        }
                        if (underlined) {
                            this.drawLine(new Vector2d(0.0, charData.height - 15.0), new Vector2d(charData.width, charData.height - 15.0), 3.0f);
                        }
                        x += charData.width - 8.0f;
                    }
                }
            }
            if (autoBegin) {
                end();
            }
        }
    }

    public float getWidth(final String text) {
        float width = 0.0f;
        CharacterData[] characterData = this.characterData;
        for (int length = text.length(), i = 0; i < length; ++i) {
            final char character = text.charAt(i);
            final char previous = (i > 0) ? text.charAt(i - 1) : '.';
            if (previous != colorCode) {
                if (character != colorCode) {
                    if (character >= characterData.length) {
                        continue;
                    }
                    final CharacterData charData = characterData[character];
                    width += (charData.width - 8.0f) / 2.0f;
                }
            }
        }
        return width + 2.0f;
    }

    public float getHeight(final String text) {
        float height = 0.0f;
        CharacterData[] characterData = this.characterData;
        for (int length = text.length(), i = 0; i < length; ++i) {
            final char character = text.charAt(i);
            final char previous = (i > 0) ? text.charAt(i - 1) : '.';
            if (previous != colorCode) {
                if (character != colorCode) {
                    if (character >= characterData.length) {
                        continue;
                    }
                    final CharacterData charData = characterData[character];
                    height = Math.max(height, charData.height);
                }
            }
        }
        return height / 2.0f - 2.0f;
    }

    private void drawChar(final char character, final CharacterData[] characterData, float x, float y, Color color, boolean dropShadow) {
        if (character >= characterData.length) {
            return;
        }
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        final CharacterData charData = characterData[character];
        float par1;
        float par2;
        float posX = charData.positionX-4;
        float posY = charData.positionY;
        float texWidth = charData.width;
        float texHeight = charData.height;
        if (dropShadow) {
            x += 1;
            y += 1;
            par1 = x + charData.width;
            par2 = y + charData.height;
            worldrenderer.pos(x, par2, 0).tex(posX * texCoordX, (posY + texHeight) * texCoordY).color(Color.black).endVertex();
            worldrenderer.pos(par1, par2, 0).tex((posX + texWidth) * texCoordX, (posY + texHeight) * texCoordY).color(Color.black).endVertex();
            worldrenderer.pos(par1, y, 0).tex((posX + texWidth) * texCoordX, posY * texCoordY).color(Color.black).endVertex();
            worldrenderer.pos(x, y, 0).tex(posX * texCoordX, posY * texCoordY).color(Color.black).endVertex();
            x -= 1;
            y -= 1;
        }
        par1 = x + charData.width;
        par2 = y + charData.height;
        worldrenderer.pos(x, par2, 0).tex(posX*texCoordX, (posY+texHeight)*texCoordY).color(color).endVertex();
        worldrenderer.pos(par1, par2, 0).tex((posX+texWidth)*texCoordX, (posY+texHeight)*texCoordY).color(color).endVertex();
        worldrenderer.pos(par1, y, 0).tex((posX+texWidth)*texCoordX, posY*texCoordY).color(color).endVertex();
        worldrenderer.pos(x, y, 0).tex(posX*texCoordX, posY*texCoordY).color(color).endVertex();
    }

    public void drawStringWithShadow(final String text, final float x, final float y, final int color) {
        this.drawStringWithShadow(text, x, y, new Color(color));
    }

    public void drawStringWithShadow(final String text, final float x, final float y, final Color color) {
        this.renderString(text, (int)x, (int)y, color, true);
    }


    public void drawLine(final Vector2d start, final Vector2d end, final float width) {
        GL11.glDisable(3553);
        GL11.glLineWidth(width);
        GL11.glBegin(1);
        GL11.glVertex2d(start.x, start.y);
        GL11.glVertex2d(end.x, end.y);
        GL11.glEnd();
        GL11.glEnable(3553);
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

    public void begin() {
        begin(770,771, 0, 0, 0, 1);
    }
    public void begin(int srcFactor, int dstFactor, float r, float g, float b, float a) {
        GlStateManager.pushMatrix();
        GlStateManager.forceBindTexture(textureId());
        GlStateManager.scale(0.5f, 0.5f, 1.0);

        GlStateManager.enableBlend();

        GlStateManager.blendFunc(srcFactor, dstFactor);
        GL14.glBlendColor(r,g,b,a);
        //GL20.glBlendEquationSeparate(770, 771);
        //GlStateManager.blendFunc(770, 772);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
    }
    public void end() {
        Tessellator.getInstance().draw();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }
    public boolean autoBegin() {
        return autoBegin;
    }

    public void setAutoBegin(boolean _autoBegin) {
        autoBegin = _autoBegin;
    }

    public String trimStringToWidth(String text, int width) {
        return this.trimStringToWidth(text, width, false);
    }

    /**
     * Trims a string to a specified width, and will reverse it if par3 is set.
     */
    public String trimStringToWidth(String text, int width, boolean reverse) {
        StringBuilder stringbuilder = new StringBuilder();
        float f = 0.0F;
        int i = reverse ? text.length() - 1 : 0;
        int j = reverse ? -1 : 1;
        boolean flag = false;
        boolean flag1 = false;

        for (int k = i; k >= 0 && k < text.length() && f < (float) width; k += j) {
            char c0 = text.charAt(k);
            float f1 = characterData[c0].width;

            if (flag) {
                flag = false;

                if (c0 != 108 && c0 != 76) {
                    if (c0 == 114 || c0 == 82) {
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

            if (f > (float) width) {
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
    public static class CharacterData {
        public char character;
        public float positionX, positionY;
        public float width;
        public float height;
        public int textureId;

        public CharacterData(final char character, final float positionX, final float positionY, final float width, final float height) {
            this.character = character;
            this.positionX = positionX;
            this.positionY = positionY;
            this.width = width;
            this.height = height;
        }
    }

    public int textureId() {
        return textureId;
    }

    public enum Style {
        REGULAR, BOLD, ITALIC
    }
}
