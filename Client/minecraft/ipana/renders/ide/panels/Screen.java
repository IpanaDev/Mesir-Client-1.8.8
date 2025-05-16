package ipana.renders.ide.panels;

import ipana.utils.font.FontHelper;
import ipana.utils.render.RenderUtils;

import java.awt.*;

public abstract class Screen {
    private Element[] elements;
    private float x,y;
    private boolean shouldRemove;

    public Screen(float x, float y,Element[] elements) {
        this.x = x;
        this.y = y;
        this.elements = elements;
    }

    public Element[] getElements() {
        return elements;
    }

    public void setElements(Element[] _elements) {
        elements = _elements;
    }

    public float getY() {
        return y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public double getWidth() {
        double width = 0;
        for (Element element : getElements()) {
            double width2 = FontHelper.SIZE_18.getWidth(element.name);
            if (width < width2) {
                width = width2;
            }
        }
        return width;
    }
    public double getHeight() {
        return getElements().length*10;
    }
    public void draw(int mouseX, int mouseY) {
        double width = getWidth();
        double height = getHeight();
        RenderUtils.drawFixedRect(x-2,y-2,x+width+2,y+height+2, Color.black);
        RenderUtils.drawFixedRect(x-1,y-1,x+width+1,y+height+1, Color.lightGray);
        float y = this.y;
        for (Element element : getElements()) {
            if (isHovered(mouseX, mouseY,y,10)) {
                RenderUtils.drawFixedRect(x,y,x+width,y+10, Color.red);
            }
            FontHelper.SIZE_18.drawString(element.getName(),x,y+2,Color.white.getRGB());
            y+=10;
        }
    }

    public abstract void onClick(int mouseX, int mouseY, int button);

    public abstract void onPress(int key, char typedChar);

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX > x && mouseX < x+getWidth() && mouseY > y && mouseY < y+getHeight();
    }
    public boolean isHovered(int mouseX, int mouseY, double y, double height) {
        return mouseX > x && mouseX < x+getWidth() && mouseY > y && mouseY < y+height;
    }

    public boolean isShouldRemove() {
        return shouldRemove;
    }

    public void setShouldRemove(boolean shouldRemove) {
        this.shouldRemove = shouldRemove;
    }

    public static class Element {
        private String name;

        public Element(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
