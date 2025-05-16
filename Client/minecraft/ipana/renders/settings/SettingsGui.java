package ipana.renders.settings;

import ipana.Ipana;
import ipana.renders.settings.anticheat.AntiCheatGui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SettingsGui extends GuiScreen {
    private List<Button> list = new ArrayList<>();
    private FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
    public static DrawGui drawGui;

    public SettingsGui() {
        list.add(new Button("BindManager",2,2,100,12));
        list.add(new Button("DrawGui",2,22,100,12));
        list.add(new Button("NBTGui",2,42,100,12));
        list.add(new Button("FriendGui",2,62,100,12));
        //list.add(new Button("AntiCheat",2,102,100,12));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        for (Button button : list) {
            button.drawButton(mouseX,mouseY);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (Button button : list) {
            if (button.hovered(mouseX,mouseY)) {
                switch (button.getName()) {
                    case "DrawGui" -> {
                        if (drawGui == null) {
                            drawGui = new DrawGui();
                        }
                        mc.displayGuiScreen(drawGui);
                    }
                    case "BindManager" -> mc.displayGuiScreen(new BindManagerGui());
                    case "NBTGui" -> mc.displayGuiScreen(Ipana.nbtGui);
                    case "FriendGui" -> mc.displayGuiScreen(new FriendGui());
                    //case "AntiCheat" -> mc.displayGuiScreen(Ipana.antiCheatGui);
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public class Button {
        private String name;
        private int x,y,width,height;

        public void drawButton(int mouseX,int mouseY) {
            Gui.drawRect(x-1,y-1,x+width+1,y+height+1,new Color(255,255,255).getRGB());
            Gui.drawRect(x,y,x+width,y+height,new Color(175,175,175).getRGB());
            if (hovered(mouseX,mouseY)) {
                Gui.drawRect(x,y,x+width,y+height,new Color(175,100,100,100).getRGB());
            }

            int w = font.getStringWidth(name)/2;
            int x1 = x+width/2-w;
            font.drawStringWithShadow(name,x1,y+2,new Color(200,2,2).getRGB());
        }
        private boolean hovered(int mouseX,int mouseY) {
            return mouseX >= x && mouseX <= x+width && mouseY >= y && mouseY <= y+height;
        }
        public Button(String name, int x, int y, int width, int height) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
