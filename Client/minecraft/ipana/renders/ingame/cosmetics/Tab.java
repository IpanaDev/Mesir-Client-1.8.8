package ipana.renders.ingame.cosmetics;

import net.minecraft.client.Minecraft;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Tab {
    String tabName;
    List<Element> elements;

    public Tab(String tabName, Consumer<Element> onClickNone, Element... element) {
        elements = new ArrayList<>();
        this.tabName = tabName;
        elements.add(new Element("None", a -> {}, onClickNone));
        Collections.addAll(elements, element);
    }
    public Tab(String tabName, Consumer<Element> onClickNone, ArrayList<Element> element) {
        elements = new ArrayList<>();
        this.tabName = tabName;
        elements.add(new Element("None", a -> {}, onClickNone));
        elements.addAll(element);
    }
    public void draw(int x, int y, int mouseX, int mouseY) {
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(tabName, x, y, Color.white.getRGB());

        int renderY = y;
        for (Element element : elements) {
            renderY+=15;
            element.draw(x+20, renderY, mouseX, mouseY);
        }
    }

    public void mouseClicked(int y, int mouseX, int mouseY) {
        int renderY = y;
        for (Element element : elements) {
            renderY+=15;
            element.mouseClicked(renderY, mouseX, mouseY);
        }
    }
}
