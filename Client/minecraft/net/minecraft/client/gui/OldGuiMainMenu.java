package net.minecraft.client.gui;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ipana.Ipana;
import ipana.utils.font.FontHelper;
import ipana.utils.render.MainMenu;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;


public class OldGuiMainMenu extends GuiScreen implements GuiYesNoCallback
{
    private boolean heheboi = false;
    private boolean updateCancelled;
    private boolean updating;
    private int kkk = 0;
    public static double percent;
    private List<Button> buttons = new ArrayList<>();

    public OldGuiMainMenu() {
        buttons.add(new Button("SinglePlayer",70,15));
        buttons.add(new Button("MultiPlayer",70,15));
        buttons.add(new Button("Changelog",70,15));
        buttons.add(new Button("Accounts",70,15));
        buttons.add(new Button("Replays",70,15));
        buttons.add(new Button("Background",70,15));
        buttons.add(new Button("Options",70,15));
        buttons.add(new Button("Quit Game",70,15));
    }



    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = RenderUtils.SCALED_RES;
        if (Ipana.background == 4) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("mesir/mainmenu/mainmenu" + Ipana.background + ".png"));
            drawModalRectWithCustomSizedTexture(-1177 / 2 - 372 - mouseX + sr.getScaledWidth(), (int) (-34 / 2 + 8 - mouseY / 9.5f + sr.getScaledHeight() / 19 - 19), 0, 0, 3841 / 2, 1194 / 2, 3841 / 2f, 1194 / 2f);
        } else {
            RenderUtils.drawImage(0, 0, sr.getScaledWidth(), sr.getScaledHeight(), new ResourceLocation("mesir/mainmenu/mainmenu" + Ipana.background + ".png"));
        }
        //RenderUtils.drawEnd(0,0,0,0,1);
        //UsersGui.INSTANCE.drawScreen(mouseX, mouseY, partialTicks);
        if (Ipana.background == 7) {
            double speed = 2;
            if (MainMenu.INSTANCE.mX) {
                MainMenu.INSTANCE.gifX+=speed;
                if (MainMenu.INSTANCE.gifX+350-112-42 > sr.getScaledWidth()) {
                    MainMenu.INSTANCE.mX = false;
                }
            } else {
                MainMenu.INSTANCE.gifX-=speed;
                if (MainMenu.INSTANCE.gifX < 0) {
                    MainMenu.INSTANCE.mX = true;
                }
            }

            if (MainMenu.INSTANCE.mY) {
                MainMenu.INSTANCE.gifY+=speed;
                if (MainMenu.INSTANCE.gifY+200 > sr.getScaledHeight()) {
                    MainMenu.INSTANCE.mY = false;
                }
            } else {
                MainMenu.INSTANCE.gifY-=speed;
                if (MainMenu.INSTANCE.gifY < 0) {
                    MainMenu.INSTANCE.mY = true;
                }
            }
            Minecraft.getMinecraft().getTextureManager().bindTexture(MainMenu.INSTANCE.menu7.getFrame(90));
            drawModalRectWithCustomSizedTexture(MainMenu.INSTANCE.gifX, MainMenu.INSTANCE.gifY, 77, 0, 350-112-42, 200, 350, 200);
        }
        {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            String text = "";
            if (calendar.get(Calendar.DAY_OF_MONTH) == 1 && calendar.get(Calendar.MONTH) == Calendar.JANUARY) {
                text = "Happy Birthday IpanaDev & LeeSin!";
            } else if (calendar.get(Calendar.DAY_OF_MONTH) == 27 && calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
                text = "THEY JUST DESTROYED PVP2!";
            } else if (calendar.get(Calendar.DAY_OF_MONTH) == 20 && calendar.get(Calendar.MONTH) == Calendar.DECEMBER) {
                text = "Happy Birthday Ghost!";
            } else if (calendar.get(Calendar.DAY_OF_MONTH) == 18 && calendar.get(Calendar.MONTH) == Calendar.JULY) {
                text = "Happy Birthday Teo!";
            }  else if (calendar.get(Calendar.DAY_OF_MONTH) == 3 && calendar.get(Calendar.MONTH) == Calendar.AUGUST) {
                text = "Happy Birthday Hawker!";
            }
            if (!text.equals("")) {
                int x = sr.getScaledWidth() / 2 - mc.fontRendererObj.getStringWidth(text)/2;
                mc.fontRendererObj.drawStringWithShadow(text, x, 2, new Color(255, 1, 1).getRGB());
            }
        }

        boolean ok = false;
        int M = ok ?  new Color(50, 50, 50).getRGB() : new Color(255, 75, 75).getRGB();
        int esir = ok ?  new Color(100, 100, 100).getRGB() : new Color(150, 150, 150).getRGB();
        GlStateManager.pushMatrix();
        GlStateManager.color(1,1,1,1);
        if (Ipana.background == 4) {
            Gui.drawRect(sr.getScaledWidth()/2f - 50-2,133,sr.getScaledWidth()/2f + 50, 283, Integer.MIN_VALUE);
            FontHelper.SIZE_18.drawStringWithShadow("Safe "+Ipana.version,sr.getScaledWidth()/2f - 50,135,Color.red.getRGB());
            getButton("SinglePlayer").setPosition(sr.getScaledWidth()/2 - 35, 145);
            getButton("MultiPlayer").setPosition(sr.getScaledWidth()/2 - 35, 165);
            getButton("Changelog").setPosition(sr.getScaledWidth()/2 - 35, 185);
            getButton("Accounts").setPosition(sr.getScaledWidth()/2 - 35, 205);
            getButton("Background").setPosition(sr.getScaledWidth()/2 - 35, 225);
            getButton("Options").setPosition(sr.getScaledWidth()/2 - 35, 245);
            getButton("Quit Game").setPosition(sr.getScaledWidth()/2 - 35, 265);
        } else {
            getButton("SinglePlayer").setPosition(sr.getScaledWidth() - 80, 5);
            getButton("MultiPlayer").setPosition(sr.getScaledWidth() - 80, 25);
            getButton("Changelog").setPosition(sr.getScaledWidth() - 80, 45);
            getButton("Accounts").setPosition(sr.getScaledWidth() - 80, 65);
            getButton("Replays").setPosition(sr.getScaledWidth() - 80, 85);
            getButton("Background").setPosition(sr.getScaledWidth() - 80, sr.getScaledHeight() - 65);
            getButton("Options").setPosition(sr.getScaledWidth() - 80, sr.getScaledHeight() - 45);
            getButton("Quit Game").setPosition(sr.getScaledWidth() - 80, sr.getScaledHeight() - 25);
        }
        for (Button button : buttons) {
            button.render(mouseX, mouseY);
        }
        GlStateManager.scale(5, 5, 5);
        mc.fontRendererObj.drawBorderedString("M", 2, 1, M,0.3f);
        GlStateManager.scale(0.5, 0.5, 0.5);
        mc.fontRendererObj.drawBorderedString("esir", 15, 9, esir,0.3f);
        GlStateManager.scale(0.5, 0.5, 0.5);
        mc.fontRendererObj.drawBorderedString(Ipana.version, 30, 10, new Color(255, 250, 250).getRGB(),0.5f);
        GlStateManager.popMatrix();
        //GuiInventory.drawEntityOnScreen(sr.getScaledWidth()/2,sr.getScaledHeight()/2,50,mouseX,mouseY,mc.thePlayer);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }


    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        //UsersGui.INSTANCE.onMouseRelease(mouseX, mouseY, state);
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ScaledResolution sr = RenderUtils.SCALED_RES;
        //UsersGui.INSTANCE.onMouseClicked(mouseX, mouseY, mouseButton);
        //button
        for (Button button : buttons) {
            if (button.isHovered(mouseX,mouseY)) {
                switch (button.name) {
                    case "SinglePlayer": {
                        mc.displayGuiScreen(new GuiSelectWorld(this));
                        break;
                    }
                    case "MultiPlayer": {
                        mc.displayGuiScreen(new GuiMultiplayer(this));
                        break;
                    }
                    case "Changelog": {
                        mc.displayGuiScreen(Ipana.changelogUI);
                        break;
                    }
                    case "Accounts": {
                        mc.displayGuiScreen(Ipana.accountManager);
                        break;
                    }
                    case "Background": {
                        Ipana.background++;
                        if (Ipana.background > 9) {
                            Ipana.background = 1;
                        }
                        break;
                    }
                    case "Options": {
                        mc.displayGuiScreen(new GuiOptions(this,mc.gameSettings));
                        break;
                    }
                    case "Quit Game": {
                        mc.shutdown();
                        break;
                    }
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_P) {
            Ipana.play=!Ipana.play;
            System.out.println("lay : "+Ipana.play);
        }
        super.keyTyped(typedChar, keyCode);
    }

    public Button getButton(String name) {
        Button button = null;
        for (Button button1 : buttons) {
            if (button1.name.equals(name)) {
                button = button1;
            }
        }
        return button;
    }

    public class Button {
        private String name;
        private int x,y,width,height;

        public Button(String name,int width,int height) {
            this.name = name;
            this.width = width;
            this.height = height;
        }

        public void render(int mouseX, int mouseY) {
            boolean ok = false;
            int normalHover = ok ? new Color(125, 125, 125).getRGB() : new Color(255, 75, 75).getRGB();
            drawBorderedRect(x, y, x+width, y+height, 2, normalHover, new Color(200, 200, 200).getRGB());
            mc.fontRendererObj.drawString(name, x+(width/2f-mc.fontRendererObj.getStringWidth(name)/2f), y+3.5, normalHover);
            if (isHovered(mouseX,mouseY)) {
                drawBorderedRect(x, y, x+width, y+height, 2, new Color(200, 200, 200).getRGB(), normalHover);
                mc.fontRendererObj.drawString(name, x+(width/2f-mc.fontRendererObj.getStringWidth(name)/2f), y+3.5, new Color(255, 255, 255).getRGB());
            }
        }
        public boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x+width && mouseY >= y && mouseY <= y+height;
        }
        public void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void drawBorderedRect(float x, float y, float x2, float y2, float l1, int col1, int col2)
    {
        drawRect(x, y, x2, y2, col2);
        float f = (col1 >> 24 & 0xFF) / 255.0F;
        float f2 = (col1 >> 16 & 0xFF) / 255.0F;
        float f3 = (col1 >> 8 & 0xFF) / 255.0F;
        float f4 = (col1 & 0xFF) / 255.0F;
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(2848);
        GL11.glPushMatrix();
        GL11.glColor4f(f2, f3, f4, f);
        GL11.glLineWidth(l1);
        GL11.glBegin(1);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x2, y);
        GL11.glVertex2d(x, y2);
        GL11.glVertex2d(x2, y2);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glDisable(2848);
    }}
