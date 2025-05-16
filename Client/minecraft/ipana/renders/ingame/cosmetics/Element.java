package ipana.renders.ingame.cosmetics;

import ipana.Ipana;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.function.Consumer;

public class Element {
    String elementName;
    Consumer<Element> onDraw;
    Consumer<Element> onClick;
    int x,y;

    public Element(String elementName, Consumer<Element> onDraw, Consumer<Element> onClick) {
        this.elementName = elementName;
        this.onDraw = onDraw;
        this.onClick = onClick;
    }

    public void draw(int x, int y, int mouseX, int mouseY) {
        this.x = x;
        this.y = y;
        if (hovered(mouseX, mouseY)) {
            RenderUtils.drawFixedRectWH(RenderUtils.SCALED_RES.getScaledWidth()-200, y-3, 200, 13, Ipana.getClientColor());
        }
        GlStateManager.color(1,1,1,1);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x,y+10,0);
        float scale = 0.9f;
        GlStateManager.scale(scale,scale,scale);
        onDraw.accept(this);
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(elementName, x+10,y, Color.white.getRGB());

    }

    public boolean hovered(int mouseX, int mouseY) {
        return mouseX >= RenderUtils.SCALED_RES.getScaledWidth()-200 && mouseY >= y-3 && mouseY < y+10;
    }

    public void mouseClicked(int y, int mouseX, int mouseY) {
        this.y = y;
        if (hovered(mouseX, mouseY)) {
            onClick.accept(this);
        }
    }
}
