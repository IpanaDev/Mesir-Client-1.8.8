package ipana.renders.settings.nbt;

import ipana.utils.font.FontHelper;
import ipana.utils.render.RenderUtils;

import java.awt.*;

public class NbtButton {
    private String name;
    private int width;
    private int height;
    private int renderingX,renderingY;

    public NbtButton(String name, int width,int height) {
        this.name = name;
        this.width = width;
        this.height = height;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getRenderingX() {
        return renderingX;
    }

    public void setRenderingX(int renderingX) {
        this.renderingX = renderingX;
    }

    public int getRenderingY() {
        return renderingY;
    }

    public void setRenderingY(int renderingY) {
        this.renderingY = renderingY;
    }

    public boolean isHovered(int mouseX, int mouseY) {
        return mouseX >= renderingX && mouseX <= renderingX+width && mouseY >= renderingY && mouseY <= renderingY+height;
    }

    public void render(int x,int y,int mouseX,int mouseY) {
        renderingX = x;
        renderingY = y;
        RenderUtils.drawFixedRect(x,y,x+width,y+height, Color.lightGray);
        if (isHovered(mouseX, mouseY)) {
            RenderUtils.drawFixedRect(x,y,x+width,y+height, new Color(200,150,150,150));
        }
        FontHelper.SIZE_18.drawStringWithShadow(name,x+(width/2f - FontHelper.SIZE_18.getWidth(name)/2),y+2,Color.red.getRGB());
    }

}
