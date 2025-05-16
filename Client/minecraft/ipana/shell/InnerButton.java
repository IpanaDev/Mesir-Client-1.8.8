package ipana.shell;

import ipana.utils.font.FontUtil;
import ipana.utils.render.RenderUtils;

import java.awt.*;

public abstract class InnerButton {
    private String name;
    private int x,y,width,height;
    private FontUtil font;

    public InnerButton(String name, int x, int y, int width, int height,FontUtil font) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.font = font;
    }
    public void renderButton(int x,int y,int mouseX, int mouseY, Color color) {
        setPosition(x, y);
        //RenderUtils.drawRoundedRect(x,y,width,height,3.5f,color);
        if (isHovered(mouseX, mouseY)) {
            Color newColor = new Color(255,255,255,100);
            RenderUtils.drawRoundedRect(x,y,width,height,3.5f,newColor);
        }
        font.drawStringWithShadow(name,x+1.9f,y+2.2f,Color.white.getRGB());
    }
    private void setPosition(int x,int y) {
        this.x = x;
        this.y = y;
    }

    abstract void isClicked(int mouseX, int mouseY);

    public boolean isHovered(int mouseX,int mouseY) {
        return mouseX>=x && mouseY>=y && mouseX<=x+width && mouseY<=y+height;
    }
}
