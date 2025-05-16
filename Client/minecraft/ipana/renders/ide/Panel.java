package ipana.renders.ide;

import ipana.renders.ide.panels.Screen;
import ipana.renders.ide.settings.SharedAttributes;
import ipana.renders.ide.util.Formatter;

import java.util.ArrayList;
import java.util.List;

public abstract class Panel {
    private float x,y;
    private String width, height;
    private List<Screen> screens = new ArrayList<>();

    public Panel(float x1, float y1, float width1, float height1) {
        this.x = x1;
        this.y = y1;
        this.width = width1+"";
        this.height = height1+"";
        initPanel();
    }
    public Panel(float x1, float y1, String width1, String height1) {
        this.x = x1;
        this.y = y1;
        this.width = width1;
        this.height = height1;
        initPanel();
    }

    public abstract void initPanel();

    public abstract void draw(int mouseX, int mouseY);

    public abstract void mouseClicked(int mouseX, int mouseY, int button);

    public abstract void keyPressed(int key, char typedChar);

    public void updateSizes() {
        if (isWindowWidth()) {
            width = "window";
        }
        if (isWindowHeight()) {
            height = "window";
        }
    }

    private boolean isWindowWidth() {
        return width.equalsIgnoreCase("window") || getWidth() == SharedAttributes.WINDOW_WIDTH;
    }
    private boolean isWindowHeight() {
        return height.equalsIgnoreCase("window") || getHeight() == SharedAttributes.WINDOW_HEIGHT;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public double getWidth() {
        return Formatter.formatWidth(width);
    }

    public void setWidth(double width) {
        this.width = width+"";
    }

    public double getHeight() {
        return Formatter.formatHeight(height);
    }

    public void setHeight(double height) {
        this.height = height+"";
    }

    public List<Screen> getActiveScreens() {
        return screens;
    }
}
