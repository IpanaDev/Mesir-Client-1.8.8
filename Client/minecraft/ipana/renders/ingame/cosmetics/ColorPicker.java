package ipana.renders.ingame.cosmetics;

import ipana.utils.Timer;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static net.minecraft.client.renderer.GlStateManager.*;
import static org.lwjgl.input.Keyboard.*;
import static org.lwjgl.opengl.GL11.*;

public class ColorPicker {
    private final Timer timer = new Timer();
    public int currentValue;
    public int x , y , width , height , color;
    private boolean typing;
    public String hex;
    public Color currentColor;
    private FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
    private int lastMouseX , lastMouseY;
    private Consumer<ColorPicker> consumer;
    private boolean dragging;

    private Color getHoverColor() {
        final ByteBuffer rgb = BufferUtils.createByteBuffer(100);
        // create buffer 100b
        GL11.glReadPixels(Mouse.getX(), Mouse.getY(), 1, 1, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, rgb);
        // read pixels (faster than robot)
        Color read = new Color(rgb.get(0) & 0xFF, rgb.get(1) & 0xFF, rgb.get(2) & 0xFF);
        if (read.getRGB() == -2173) return read.brighter(); // LOL
        return read; // % 255 to avoid problems...
    }

    public ColorPicker(Consumer<ColorPicker> consumer, int savedColor) {
        this.consumer = consumer;
        this.color = savedColor;
    }

    public void draw(int x, int y, int width, int height, int mouseX, int mouseY, Color currentColor) {
        draw(x, y, width, height, mouseX, mouseY, currentColor, true);
    }

    public void drawRect(double left, double top, double right, double bottom, int color) {
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
        float f3 = (color >> 24 & 255) / 255.0F;
        float f = (color >> 16 & 255) / 255.0F;
        float f1 = (color >> 8 & 255) / 255.0F;
        float f2 = (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        enableBlend();
        disableTexture2D();
        tryBlendFuncSeparate(770, 771, 1, 0);
        color(f, f1, f2, f3);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(left, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, bottom, 0.0D).endVertex();
        worldrenderer.pos(right, top, 0.0D).endVertex();
        worldrenderer.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
        enableTexture2D();
        disableBlend();
        resetColor();
    }

    public void draw(int x, int y, int width, int height, int mouseX, int mouseY, Color currentColor, boolean isFront) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        final float f = (color >> 16 & 255) / 255.0F;
        final float f1 = (color >> 8 & 255) / 255.0F;
        final float f2 = (color & 255) / 255.0F;
        final double h = 1;
        for (int i = 0; i < height; i++) {
            drawRect(x + width+1, y + (h * i), x + width + 11, y + (h * (i + 1)), Color.HSBtoRGB((float) i / height, 1, 1));
            if (isFront && Mouse.isButtonDown(0) && mouseX >= x + width + 1 && mouseX <= x + width + 11 && mouseY >= y + (h * i) && mouseY <= y + (h * (i + 1))) {
                color = Color.HSBtoRGB((float) i / height, 1, 1);
            }
        }
        for (int i = 0; i < height; i++) {
            if (color == Color.HSBtoRGB((float) i / height, 1, 1)) {
                drawRect(x + width + 1, y + (h * i) + 1, x + width + 11, y + (h * (i + 1)) + 2, Color.black.getRGB());
                drawRect(x + width + 1, y + (h * i) - 2, x + width + 11, y + (h * (i + 1)) - 1, Color.black.getRGB());
            }
        }
        GlStateManager.enableBlend();
        glEnable(GL_BLEND);
        glShadeModel(GL_SMOOTH);
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_TEXTURE_2D);
        GL11.glBegin(GL_QUADS);
        glColor(new Color(f, f1, f2));
        glVertex2d(x + width, y);
        glColor(Color.white);
        glVertex2d(x, y);
        glColor(Color.BLACK);
        glVertex2d(x, y + height);
        glColor(Color.BLACK);
        glVertex2d(x + width, y + height);
        GL11.glEnd();
        glEnable(GL_TEXTURE_2D);
        if ((isFront && Mouse.isButtonDown(0)) && isHover(mouseX, mouseY)) {
            dragging = true;
            final int hoverColor = getHoverColor().getRGB();
            currentValue = hoverColor;
            this.currentColor = getHoverColor();
            hex = Integer.toHexString(hoverColor).substring(2);
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        } else if (dragging) {
            consumer.accept(this);
            dragging = false;
        }
        //		if (currentValue != color) {
        //			color = currentValue;
        //		}
        try {
            if (hex == null) {
                hex = Integer.toHexString(currentValue).substring(2);
            }
        }
        catch (Exception e) {
            hex = "No color...";
        }
        RenderUtils.drawBorderedRect(lastMouseX - 2, lastMouseY - 2, lastMouseX + 2, lastMouseY + 2, 1D, currentValue, Color.BLACK.getRGB(), true);
        drawRect(x + width + 16, y, x + width + 60, y + 14, currentColor.getRGB());
        //		drawRect(x + width + 25, y + 27 - font.getHeight(), x + width + 85, y + 25, Integer.MIN_VALUE);
        font.drawStringWithShadow("#" + hex + (typing && !timer.delay(250) ? "_" : ""), x + width + 17, y + 21 - font.FONT_HEIGHT * 2, Color.WHITE.getRGB());
        timer.delay(500L, func -> {
            // wish there was something to do lol
        }, true);
        typing = false; // TODO add typing?
    }

    public static void glColor(final Color color) {
        GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F, color.getAlpha() / 255F);
    }

    public void handleInput(char typedChar, int keyCode) {
        try {
            if (typing) {
                int digit = Character.digit(typedChar, 16);
                int limit = -Integer.MIN_VALUE;
                if (hex.length() > 0 && hex.charAt(0) == '-') {
                    limit = Integer.MIN_VALUE;
                }
                int multmin = limit / 16;
                switch (keyCode) {
                    case KEY_BACK:
                        if (hex.length() > 0) {
                            hex = hex.substring(0, hex.length() - 1);
                            if (hex.replace("-", "").length() > 0) {
                                currentValue = Integer.parseInt(hex, 16);
                            }
                        }
                        break;
                    case KEY_END, KEY_RETURN: // cool as fuck
                        typing = false;
                        if (hex.replace("-", "").length() > 0) {
                            currentValue = Integer.parseInt(hex, 16);
                        }
                        break;
                    default:
                        if ((typedChar == '-' || digit >= 0)) {
                            hex = hex + typedChar;
                            if (hex.replace("-", "").length() > 0) {
                                currentValue = Integer.parseInt(hex, 16);
                            }
                        }
                        break;
                }
            }
        }
        catch (NumberFormatException e) {
            if (hex.length() > 0) {
                hex = hex.substring(0, hex.length() - 1);
            }
        }
    }

    public boolean isHover(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }
}
