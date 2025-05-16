package ipana.irc.user;


import java.awt.*;
import java.util.*;
import java.util.List;

import ipana.Ipana;
import ipana.irc.*;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.*;
import net.minecraft.util.*;

import static ipana.utils.render.RenderUtils.SCALED_RES;

public class UsersGui {
    public final IRC irc;
    private static final int RGB10 = new Color(5, 5, 5, 255).getRGB();
    private static final int RGB9 = new Color(40, 40, 40, 255).getRGB();
    private static final int RGB8 = new Color(22, 22, 22, 255).getRGB();
    private static final int RGB7 = new Color(60, 60, 60, 255).getRGB();
    private static final int RGB6 = new Color(9, 9, 9, 255).getRGB();
    private boolean isRendering;
    public List<Tuple<String[], Float>> list = new ArrayList<>();
    private List<Float> floatList = new ArrayList<>();
    public long ms = System.currentTimeMillis();
    public boolean dragging;
    public int dragX , dragY;
    public int prevDragX , prevDragY;
    private Minecraft mc = Minecraft.getMinecraft();

    public UsersGui(IRC irc) {
        this.irc = irc;
        isRendering = true;
        dragX = 20;
        dragY = 10;
        prevDragX = dragX;
        prevDragY = dragY;
    }

    public int getSize(final boolean height) {

        floatList.clear();
        list.forEach(salakghost -> floatList.add(salakghost.getSecond()));
        if (!floatList.isEmpty()) {
            Collections.sort(floatList);
            Collections.reverse(floatList);
        }
        float width = floatList.isEmpty() ? 200F : floatList.get(0) + 20;
        if (!height) return (int) (!isRendering ? 200 : width);
        return !isRendering ? 20 : list.isEmpty() ? 70 : 90 + list.size() * 25;
    }

    public int getCoords(final boolean y) {
        if (!y) return dragX;
        return dragY;
    }

    public void onMouseRelease(final int mouseX, final int mouseY, final int state) {
        if (dragging) {
            dragging = false;
        }
    }

    public void onMouseClicked(final int mouseX, final int mouseY, final int state) {
        final int x = getCoords(false);
        final int y = getCoords(true);
        final int scaledWidth = getSize(false);
        final int scaledHeight = getSize(true);
        final int height = scaledHeight - 20;
        final String a = isRendering ? "Hide users" : "Show users";
        final boolean isHovering = mouseWithinBounds(mouseX, mouseY, (int) (x - 4 + scaledWidth / 2D - mc.fontRendererObj.getStringWidth(a) / 2D), y, mc.fontRendererObj.getStringWidth(a), 12);
        final boolean hoveringAll = mouseWithinBounds(mouseX, mouseY, x, y, scaledWidth, scaledHeight);
        if (hoveringAll && !isHovering && state == 0) {
            prevDragX = x - mouseX;
            prevDragY = y - mouseY;
            dragging = true;
            return;
        }
        if (isHovering && state == 0) {
            isRendering ^= true;
        }
    }

    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        if (System.currentTimeMillis() - ms > 50L) {
            list.clear();
            List<User> users = irc.users();
            for (final User user : users) {
                final String sender = user.senderName();
                final String igName = user.getProperty(UserProperties.INGAME_NAME);
                final String client = user.getProperty(UserProperties.CLIENT);
                final String server = user.getProperty(UserProperties.SERVER);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("§c");
                stringBuilder.append(sender);
                stringBuilder.append("§f logged as §a");
                stringBuilder.append(igName);
                final String text1 = stringBuilder.toString();
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("§fClient§6: §a");
                stringBuilder2.append(client);
                stringBuilder2.append("§f in §6");
                stringBuilder2.append(server);
                final String text2 = stringBuilder2.toString();
                final float width = Math.max(mc.fontRendererObj.getStringWidth(text1), mc.fontRendererObj.getStringWidth(text2) * 0.5F) + 10;
                final Tuple<String[], Float> tuple = new Tuple<>(new String[]{
                        text1, text2
                }, width);
                list.add(tuple);
            }
            ms = System.currentTimeMillis();
        }
        if (dragging) {
            dragX = (mouseX + prevDragX);
            dragY = (mouseY + prevDragY);
            if (dragX + getSize(false) > SCALED_RES.getScaledWidth() + 20) {
                dragX = SCALED_RES.getScaledWidth() + 20 - getSize(false);
            }
            if (dragY > SCALED_RES.getScaledHeight()) {
                dragY = SCALED_RES.getScaledHeight();
            }
            if (dragY + getSize(true) < 0) {
                dragY = 0;
            }
        }
        final int RGB5 = RGB6;
        final int RGB4 = RGB7;
        final int RGB3 = RGB8;
        final int RGB2 = RGB9;
        final int RGB = RGB10;
        final int x = getCoords(false);
        int y = getCoords(true);
        final int posX = x - 10;
        final int posY = y - 6;
        final int scaledWidth = getSize(false);
        final int scaledHeight = getSize(true);
        final int height = scaledHeight - 20;
        Gui.drawRect(posX, posY, posX + scaledWidth + 2, posY + height + 20D, RGB);
        RenderUtils.drawBorderedRect(posX + .5D, posY + .5D, posX + scaledWidth + 1.5D, posY + height + 19.5, 0.5, RGB2, RGB4, true);
        RenderUtils.drawBorderedRect(posX + 2D, posY + 2D, posX + scaledWidth, posY + height + 18D, 0.5, RGB3, RGB4, true);
        Gui.drawRect(posX + 2.5, posY + 2.5, posX + scaledWidth - .5, posY + 4.5, RGB5);
        if (isRendering) {
            final String a = "Hide users";
            final boolean isHovering = mouseWithinBounds(mouseX, mouseY, (int) (x - 7 + scaledWidth / 2D - mc.fontRendererObj.getStringWidth(a) / 2D), y, mc.fontRendererObj.getStringWidth(a), 12);
            mc.fontRendererObj.drawString(a, (float) (x - 9 + scaledWidth / 2D - mc.fontRendererObj.getStringWidth(a) / 2D), y + 1, isHovering ? Color.pink.getRGB() : -1);
            final double offsetScale = 1.0F;
            /*for (int i = posX + 3; i < posX + scaledWidth - 1; ++i) {
                Gui.drawRect(i, posY + 3D, i + 1D, posY + 5D, Colors.getChromaColorCustom(i, posY + 3D, offsetScale, 3000, 0.7f, 0.99f).getRGB());
            }
            
             */
            y += 10;
            final User self = irc.self();
            mc.fontRendererObj.drawString("Current User §c" + self.senderName(), x, y, Color.darkGray.brighter().getRGB());
            for (final UserProperties property : UserProperties.VALUES) {
                if (property == UserProperties.HWID) {
                    continue;
                }
                final StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(property.name().substring(0, 1).toUpperCase());
                stringBuilder.append(property.name().substring(1).toLowerCase());
                final String fancyName = stringBuilder.toString().replace("_", " ");
                y += 12;
                mc.fontRendererObj.drawString(fancyName + " §a" + self.getProperty(property), x, y, Color.darkGray.brighter().getRGB());
            }
            if (!list.isEmpty()) {
                y += 17;
                final String text = "Online Users";
                mc.fontRendererObj.drawStringWithShadow(text, (float) (x - 8 + scaledWidth / 2D - mc.fontRendererObj.getStringWidth(text) / 2D), y, Color.WHITE.getRGB());
                y += 12;
                for (final Tuple<String[], Float> kek : list) {
                    float width = kek.getSecond();
                    String[] first = kek.getFirst();
                    String text1 = first[0];
                    String text2 = first[1];
                    final double zencigot = 23D;
                    RenderUtils.drawBorderedRect(x + .5D, y + .5D, x + width + 1.5D, y + zencigot + 1.5D, 0.5, RGB2, RGB4, true);
                    RenderUtils.drawBorderedRect(x + 2D, y + 2D, x + width, y + zencigot, 0.5, RGB3, RGB4, true);
                    y += 5;
                    mc.fontRendererObj.drawStringWithShadow(text1, x + 4, y, Color.white.getRGB());
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(x + 4, y + 11, 0);
                    final double scale = 0.5D;
                    GlStateManager.scale(scale, scale, scale);
                    mc.fontRendererObj.drawStringWithShadow(text2, 0, 0, Color.WHITE.getRGB());
                    GlStateManager.popMatrix();
                    y += 20;
                }
            }
        } else {
            float width = mc.fontRendererObj.getStringWidth("Show Users");
            int ddd = (int) (x - 7 + scaledWidth / 2D - width / 2D);
            final boolean isHovering = mouseWithinBounds(mouseX, mouseY, ddd, posY, (int) width, scaledHeight);
            mc.fontRendererObj.drawString("Show users", ddd, y + 1,
                    isHovering ? Color.pink.getRGB() : -1);
            /*
            for (var i = posX + 3; i < posX + scaledWidth - 1; ++i) {
                Gui.drawRect(i, posY + 3D, i + 1D, posY + 5D, Colors.getChromaColorCustom(i, posY + 3D, 1.0F, 3000, 0.7f, 0.99f).getRGB());
            }
            
             */
        }
    }
    
    private boolean mouseWithinBounds(int mouseX, int mouseY, int x, int y, int x2, int y2) {
        return mouseX >= x && mouseX <= x+x2 && mouseY >= y && mouseY <= y+y2;
    }
}
