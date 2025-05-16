package ipana.utils.render;

import ipana.Ipana;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class EmoteUtils {
    private static final List<Emote> LIST = new ArrayList<>();

    public static void init() {
        LIST.add(new Emote("thinking"));
        LIST.add(new Emote("sunglasses"));
        LIST.add(new Emote("middle_finger"));
        LIST.add(new Emote("kedi1"));
        LIST.add(new Emote("kedi2"));
        LIST.add(new Emote("rofl"));
        LIST.add(new Emote("smirk"));
        LIST.add(new Emote("wink"));
        LIST.add(new Emote("kapak"));
        LIST.add(new Emote("flushed"));
        LIST.add(new Emote("why"));
        LIST.add(new Emote("sasi"));
        LIST.add(new Emote("nah"));
        LIST.add(new Emote("skull"));
        LIST.add(new Emote("sehit09"));
        LIST.add(new Emote("happysehit"));
        LIST.add(new Emote("skull2"));
        LIST.add(new Emote("yorumsuz"));
        LIST.add(new Emote("hawker"));
        LIST.add(new Emote("troll"));
        LIST.add(new Emote("hawkersaz"));
        LIST.add(new Emote("br1cn"));
        LIST.add(new Emote("hako"));
        LIST.add(new Emote("grin"));
        LIST.add(new Emote("yiyinbirbirinizi",200));
        LIST.add(new Emote("ali", 50));
        LIST.add(new Emote("sictim", 20));
    }

    public static List<Emote> getList() {
        return LIST;
    }

    public static Emote getEmote(String name) {
        for (Emote emote : getList()) {
            if (emote.getName().equals(name)) {
                return emote;
            }
        }
        return null;
    }

    public static class Action {
        public Emote emote;
        public int leftTicks, prevTicks;

        public Action(Emote emote) {
            this.emote = emote;
            this.leftTicks = 60;
            this.prevTicks = leftTicks;
        }
    }

    public static class Emote {
        private String name;
        private ResourceLocation texture;
        private int animTicks;
        private long ms;
        private int animSize;
        private boolean animated;
        private int[] textureIds;
        private long refreshDelay;

        public Emote(String name) {
            this.name = name;
            this.texture = new ResourceLocation("mesir/emotes/"+this.name+".png");
            setAnimated(false);

        }
        public Emote(String name, long refreshDelay) {
            this.name = name;
            this.texture = new ResourceLocation("mesir/emotes/"+this.name+".png");
            this.refreshDelay = refreshDelay;
            try {
                InputStream inputStream = Ipana.class.getResourceAsStream("/assets/minecraft/mesir/emotes/" + this.name + ".png");
                BufferedImage image = ImageIO.read(inputStream);//anan null kanka
                setAnimated(image.getWidth() != image.getHeight());
                if (isAnimated()) {
                    animSize = image.getHeight()/image.getWidth();
                    textureIds = new int[animSize];
                    int size = image.getWidth();
                    ByteBuffer buffer = ByteBuffer.allocateDirect(size * size * 4);
                    GlStateManager.enableTexture2D();
                    for (int i = 0; i < textureIds.length; i++) {
                        int textureId = GL11.glGenTextures();
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
                        BufferedImage frame = image.getSubimage(0, i*size, size, size);
                        for (int y = 0; y < size; y++) {
                            for (int x = 0; x < size; x++) {
                                int color = frame.getRGB(x, y);
                                buffer.put((byte) (color >> 16 & 0xFF));
                                buffer.put((byte) (color >> 8 & 0xFF));
                                buffer.put((byte) (color & 0xFF));
                                buffer.put((byte) (color >> 24 & 0xFF));
                            }
                        }
                        buffer.flip();
                        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                        GL11.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
                        GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size, size, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
                        textureIds[i] = textureId;
                    }
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                    GlStateManager.bindTexture(0);
                    GlStateManager.disableTexture2D();
                }
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public void render(double x, double y, double width, double height) {
            if (!isAnimated()) {
                RenderUtils.drawImage(x, y, width, height, texture);
            } else {
                if (System.currentTimeMillis() - ms >= refreshDelay) {
                    if (++animTicks >= textureIds.length) {
                        animTicks = 0;
                    }
                    ms = System.currentTimeMillis();
                }
                RenderUtils.drawImage(x, y, width, height, textureIds[animTicks]);
            }
        }

        public void render(double x, double y) {
            render(x,y,10,10);
        }

        public void setTexture(ResourceLocation texture) {
            this.texture = texture;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isAnimated() {
            return animated;
        }

        public void setAnimated(boolean animated) {
            this.animated = animated;
        }
    }
}
