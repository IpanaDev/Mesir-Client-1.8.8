package ipana.utils.render;

import net.minecraft.client.gui.Gui;

import java.awt.*;

public class ColorChooser {
    private int x,y,width,height;
    private Color output;

    public ColorChooser(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        output = Color.white;
    }

    public void mouseClicked(int mouseX,int mouseY) {
        /* Render Green & Blue*/
        {
            int posX = 0;
            int posY = 0;
            for (int g = 0; g < 255; g += 10) {
                for (int b = 0; b < 255; b += 10) {
                    if (posY>=50+height) {
                        posX += width;
                        posY = 0;
                    }
                    if (hovered(mouseX,mouseY,posX,posY)) {
                        output = new Color(0,g,b);
                    }
                    posY += height;
                }
            }
        }
        /* Render Green & Red*/
        {
            int posX = 0;
            int posY = 0;
            for (int g = 0; g < 255; g += 10) {
                for (int r = 0; r < 255; r += 10) {
                    if (posY>=50+height) {
                        posX += width;
                        posY = 0;
                    }
                    if (hovered(mouseX,mouseY,posX,posY+100)) {
                        output = new Color(r,g,0);
                    }
                    posY += height;
                }
            }
        }
        /* Render Blue & Red*/
        {
            int posX = 0;
            int posY = 0;
            for (int b = 0; b < 255; b += 10) {
                for (int r = 0; r < 255; r += 10) {
                    if (posY>=50+height) {
                        posX += width;
                        posY = 0;
                    }
                    if (hovered(mouseX,mouseY,posX,posY+200)) {
                        output = new Color(r,0,b);
                    }
                    posY += height;
                }
            }
        }
    }

    public void drawColorPalette(int mouseX, int mouseY) {
        /* Render Green & Blue*/
        {
            int posX = 0;
            int posY = 0;
            for (int g = 0; g < 255; g += 10) {
                for (int b = 0; b < 255; b += 10) {
                    if (posY>=50+height) {
                        posX += width;
                        posY = 0;
                    }
                    Gui.drawRect(x + posX, y + posY, x + width + posX, y + height + posY, new Color(0, g, b).getRGB());
                    if (hovered(mouseX,mouseY,posX,posY)) {
                        Gui.drawRect(x + posX, y + posY, x + width + posX, y + height + posY, new Color(255,255,255,150).getRGB());
                    }
                    posY += height;
                }
            }
        }
        /* Render Green & Red*/
        {
            int posX = 0;
            int posY = 0;
            for (int g = 0; g < 255; g += 10) {
                for (int r = 0; r < 255; r += 10) {
                    if (posY>=50+height) {
                        posX += width;
                        posY = 0;
                    }
                    Gui.drawRect(x + posX, y + posY+100, x + width + posX, y + height + posY+100, new Color(r, g, 0).getRGB());
                    if (hovered(mouseX,mouseY,posX,posY+100)) {
                        Gui.drawRect(x + posX, y + posY+100, x + width + posX, y + height + posY+100, new Color(255,255,255,150).getRGB());
                    }
                    posY += height;
                }
            }
        }
        /* Render Blue & Red*/
        {
            int posX = 0;
            int posY = 0;
            for (int b = 0; b < 255; b += 10) {
                for (int r = 0; r < 255; r += 10) {
                    if (posY>=50+height) {
                        posX += width;
                        posY = 0;
                    }
                    Gui.drawRect(x + posX, y + posY+200, x + width + posX, y + height + posY+200, new Color(r, 0, b).getRGB());
                    if (hovered(mouseX,mouseY,posX,posY+200)) {
                        Gui.drawRect(x + posX, y + posY+200, x + width + posX, y + height + posY+200, new Color(255,255,255,150).getRGB());
                    }
                    posY += height;
                }
            }
        }
    }

    private boolean hovered(int mouseX, int mouseY,int posX,int posY) {
        return mouseX > x+posX && mouseX <= x+width+posX && mouseY > y+posY && mouseY <= y+height+posY;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public Color getOutput() {
        return output;
    }

    public void setOutput(Color output) {
        this.output = output;
    }
}