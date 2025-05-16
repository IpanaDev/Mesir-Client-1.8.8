package ipana.utils.font.list;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Locale;

public class ListFont {
    private Font font;
    private boolean fractionalMetrics;
    public CharacterData[] regularData;
    private CharacterData[] boldData;
    private CharacterData[] italicsData;
    private HashMap<Character, CharProperty> map = new HashMap<>();

    public void addString(String text, float x, float y, Color color, boolean shadow) {
        for (int i = 0; i < text.length(); i++) {
            char character = text.charAt(i);
            CharacterData charData = regularData[character];
            if (!map.containsKey(character)) {
                CharProperty charProperty = new CharProperty(character, shadow);
                charProperty.addPosition(x,y,color);
                map.put(character,charProperty);
            } else {
                CharProperty charProperty = map.get(character);
                charProperty.addPosition(x,y,color);
            }
            x+=charData.width-8;
        }
    }

    public void render() {
        GL11.glPushMatrix();
        GlStateManager.scale(0.5f, 0.5f, 1.0);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);

        for (CharProperty charProperty : map.values()) {
            charProperty.drawChar(regularData);
        }

        GlStateManager.disableBlend();
        GlStateManager.bindTexture(0);
        GL11.glColor4d(1.0, 1.0, 1.0, 1.0);
        GL11.glPopMatrix();
        map = new HashMap<>();
    }

    public ListFont(final Font font, final boolean disableAA) {
        this(font, 4096, disableAA);
    }

    public ListFont(final Font font, final int characterCount, final boolean disableAA) {
        this(font, characterCount, true, disableAA);
    }

    public ListFont(final Font font, final int characterCount, final boolean fractionalMetrics, final boolean disableAA) {
        long ms = System.currentTimeMillis();
        this.font = font;
        this.fractionalMetrics = fractionalMetrics;
        this.regularData = this.setup(new CharacterData[characterCount], 0, disableAA);
        this.boldData = this.setup(new CharacterData[characterCount], 1, disableAA);
        this.italicsData = this.setup(new CharacterData[characterCount], 2, disableAA);
        System.out.println("Font created: " + (System.currentTimeMillis() - ms));
    }

    private CharacterData[] setup(final CharacterData[] characterData, final int type, final boolean disableAA) {
        final Font font = this.font.deriveFont(type);
        final BufferedImage utilityImage = new BufferedImage(1, 1, 2);
        final Graphics2D utilityGraphics = (Graphics2D) utilityImage.getGraphics();
        utilityGraphics.setFont(font);
        final FontMetrics fontMetrics = utilityGraphics.getFontMetrics();
        for (int index = 0; index < characterData.length; ++index) {
            final char character = (char) index;
            final Rectangle2D characterBounds = fontMetrics.getStringBounds(String.valueOf(character), utilityGraphics);
            final float width = (float) characterBounds.getWidth() + 8.0f;
            final float height = (float) characterBounds.getHeight();
            final BufferedImage characterImage = new BufferedImage(MathHelper.ceiling_double_int(width), MathHelper.ceiling_double_int(height), 2);
            final Graphics2D graphics = (Graphics2D) characterImage.getGraphics();
            graphics.setFont(font);
            graphics.setColor(new Color(255, 255, 255, 0));
            graphics.fillRect(0, 0, characterImage.getWidth(), characterImage.getHeight());
            graphics.setColor(Color.WHITE);
            if (disableAA) {
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, this.fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
            }
            graphics.drawString(String.valueOf(character), 4, fontMetrics.getAscent());
            final int textureId = GL11.glGenTextures();
            this.createTexture(textureId, characterImage);
            characterData[index] = new CharacterData(character, characterImage.getWidth(), characterImage.getHeight(), textureId);
        }

        return characterData;
    }

    private void createTexture(final int textureId, final BufferedImage image) {
        final int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        final ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * 4);
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                final int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) (pixel >> 16 & 0xFF));
                buffer.put((byte) (pixel >> 8 & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) (pixel >> 24 & 0xFF));
            }
        }
        buffer.flip();

        GlStateManager.bindTexture(textureId);
        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
        GL11.glTexImage2D(3553, 0, 6408, image.getWidth(), image.getHeight(), 0, 6408, 5121, buffer);
    }

    public static final char colorCode = '§';
    public static final String colorIndex = "0123456789abcdefklmnor";


    public float getWidth(final String text) {
        float width = 0.0f;
        CharacterData[] characterData = this.regularData;
        for (int length = text.length(), i = 0; i < length; ++i) {
            final char character = text.charAt(i);
            final char previous = (i > 0) ? text.charAt(i - 1) : '.';
            if (previous != colorCode) {
                if (character == colorCode) {
                    final int index = colorIndex.indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));
                    if (index == 17) {
                        characterData = this.boldData;
                    } else if (index == 20) {
                        characterData = this.italicsData;
                    } else if (index == 21) {
                        characterData = this.regularData;
                    }
                } else {
                    final CharacterData charData = characterData[character];
                    width += (charData.width - 8.0f) / 2.0f;
                }
            }
        }
        return width + 2.0f;
    }

    public double getHeight(final String text) {
        double height = 0.0;
        CharacterData[] characterData = this.regularData;
        for (int length = text.length(), i = 0; i < length; ++i) {
            final char character = text.charAt(i);
            final char previous = (i > 0) ? text.charAt(i - 1) : '.';
            if (previous != colorCode) {
                if (character == colorCode) {
                    final int index = colorIndex.indexOf(text.toLowerCase(Locale.ENGLISH).charAt(i + 1));
                    if (index == 17) {
                        characterData = this.boldData;
                    } else if (index == 20) {
                        characterData = this.italicsData;
                    } else if (index == 21) {
                        characterData = this.regularData;
                    }
                } else {
                    final CharacterData charData = characterData[character];
                    height = Math.max(height, charData.height);
                }
            }
        }
        return height / 2.0 - 2.0;
    }

    public static class CharacterData {
        public char character;
        public double width;
        public double height;
        public int textureId;

        public CharacterData(final char character, final double width, final double height, final int textureId) {
            this.character = character;
            this.width = width;
            this.height = height;
            this.textureId = textureId;
        }

        public void bind() {
            //GlStateManager.forceBindTexture(textureId);
        }
    }
}