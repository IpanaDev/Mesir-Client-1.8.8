package ipana.renders;

import ipana.Ipana;
import ipana.utils.math.MathUtils;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class ChangelogUI extends GuiScreen {
    private List<Button> buttons = new ArrayList<>();
    private Button selected;

    public ChangelogUI() {
        int y = 5;
        buttons.add(new Button("Version 0.6", 5, y+=20, 75, 15,
                "Added CrowAura",
                        "Added DarkTheme",
                        "Added BowFly",
                        "Removed PortScanner",
                        "Removed GodMode"));
        buttons.add(new Button("Version 0.7", 5, y+=20, 75, 15,
                "Added InvMove",
                        "Added Changelog",
                        "Added Cape System",
                        "Added Streax Theme",
                        "Added Lucid Theme",
                        "Fixed JumpPot",
                        "Removed Teo",
                        "Redesigned Ipana Theme"));

        buttons.add(new Button("Version 0.8", 5, y+=20, 75, 15,
                "Added Damage Module",
                        "Added Delta ClickGui",
                        "Added AutoMine Gui",
                        "Added DuraTest",
                        "Added BowFly (Fly Mode)",
                        "Added LatestNCP TpAura (SetPos)",
                        "Added DarkTheme (Click Gui)",
                        "Added Reverse Colored Screen (ClickGui)",
                        "Added Wide Meme (Enable options/video settings/details/stars)",
                        "Fixed Image Bug",
                        "Fixed FPS Bugs",
                        "Fixed NullPoint CapeGui",
                        "Redesigned HurtTime (Aura)",
                        "Redesigned Rotations (Aura)",
                        "Redesigned Third Person Block Anim",
                        "Redesigned RTE2023 (Event Crit)",
                        "Redesigned TickAura (Faster Attacks)",
                        "Redesigned Crash Report Texts!",
                        "Removed ErsinBuken.class",
                        "Removed Leak Protect (Abi Nolur Leaklama)"));

        buttons.add(new Button("Version 0.9", 5, y+=20, 75, 15,
                "Added New Capes",
                        "Added Weather Module",
                        "Added LongJump Fly Mode",
                        "Added Latest Option (Speed,Fly)",
                        "Added Legit Aura Mode",
                        "Added Motion Velocity Mode",
                        "Added Straight Scaffold Mode",
                        "Added Unio Module (bok gibi aura açma)",
                        "Added ItemRenderer case3 (Swing Eat Anim)",
                        "Added Eriq's Shell Api (F4 to open)",
                        "Added Emotes for chat",
                        "Added Unicode Font (type \"#naber knk# in chat to toggle)",
                        "Fixed BindManager Crash",
                        "Fixed HurtTime Aura (Calculating damage for item and enchants)",
                        "Redesigned Vanilla Phase Mode",
                        "Redesigned MainMenu",
                        "Redesigned Tags",
                        "Redesigned Options Font",
                        "Redesigned Step (sonunda)",
                        "Redesigned D2 is now hurttime sensitive (Dura Test)",
                        "Redesigned Wide Module",
                        "Removed Obf (Abi nolur leaklama v2)"));

        buttons.add(new Button("Version 1.0", 5, y+=20, 75, 15,
                "Added Account Manager",
                        "Added Cape File Config",
                        "Added Enchants (Tags)",
                        "Redesigned ClickGui (Ipana)",
                        "Redesigned CapeGui",
                        "Redesigned LongJump",
                        "Redesigned Phase (LatestNCP)",
                        "Redesigned Hud (Ipana)",
                        "Redesigned QuickUse (Place)",
                        "Redesigned TPAura (PathFind)",
                        "Redesigned Tick Aura",
                        "Removed SetName Module",
                        "Removed Hiro-"));

        buttons.add(new Button("Version 1.1", 5, y+=20, 75, 15,
                "Added Axis",
                        "Added Herobrine",
                        "Added New Main Menu Background",
                        "Added RTE2025",
                        "Added Voice Module Toggle",
                        "Added Toggle Command For Shell",
                        "Added Value Command For Shell",
                        "Added New Emotes",
                        "Added Ghost (DiamOntiratous)",
                        "Added NoBob",
                        "Added Nuker",
                        "Added More Optimization",
                        "Fixed BindManager Mouse Scroll",
                        "Fixed Delta Click Gui Mouse Scroll",
                        "Redesigned Hud",
                        "Redesigned DrawGui",
                        "Redesigned HurtTime",
                        "Redesigned Fly Damage Packets",
                        "Redesigned OldNCPV2 Fly Mode",
                        "Redesigned Slow Phase Mode (FullBlock)",
                        "Redesigned Scaffold",
                        "Redesigned OldNCP Speed Mode",
                        "Redesigned AutoHeal",
                        "Removed Crosshair Module",
                        "Removed Crosshair Gui",
                        "Removed RTE2023",
                        "Removed Crow Aura"));

        buttons.add(new Button("Version 1.2", 5, y+=20, 75, 15,
                "Added SemoTeo",
                        "Added ProArisV2",
                        "Added Erik Hud",
                        "Added BlockAnim",
                        "Added New Emotes",
                        "Added New MainMenu Theme",
                        "Redesigned MainMenu",
                        "Redesigned Shell",
                        "Redesigned OldNCP Speed",
                        "Redesigned Phase",
                        "Redesigned ClickGui",
                        "Redesigned Value System",
                        "Removed Delta Gui",
                        "Removed AutoGapple",
                        "Removed Axis",
                        "Removed Herobrine"));

        buttons.add(new Button("Version 1.3", 5, y+=20, 75, 15,
                "Added Yelleme Edition",
                        "Added Eymen moment",
                        "Added So many optimizations",
                        "Added Dolar",
                        "Added 1050 ti",
                        "Added Snake game",
                        "Redesigned Click Gui"));

        buttons.add(new Button("Version 1.4", 5, y+=20, 75, 15,
                "Added Average Edition",
                        "Added Freecam",
                        "Added OldNCPLongJump",
                        "Added Importable Music Player (Ipana Config/musics/)",
                        "Optimized Entity Lists (NameTags, KillAura etc.)",
                        "Optimized Sky",
                        "Redesigned Solid WaterWalk",
                        "Redesigned Criticals (1 Packet yay!)",
                        "Redesigned Bow Fly (Better)",
                        "Redesigned Music Player",
                        "Removed Voice recognation (Crashes with obf)",
                        "Fixed Some bugs",
                        "Fucked TpAura dont use",
                        "Fucked Game open time (slowass mf)"));

        buttons.add(new Button("Version 1.5", 5, y+=20, 75, 15,
                "Added Axis back",
                        "Added UnitedRat",
                        "Added Ersinin Camisi",
                        "Added Token Yoinker",
                        "Added Enayilik",
                        "Added More Enayilik",
                        "Removed All shits"));

        buttons.add(new Button("Version 1.6", 5, y+=20, 75, 15,
                "Added IRC System",
                        "Added 3.11.1 Bunny",
                        "Added Amongus",
                        "Fixed AutoDrink",
                        "Redesigned Both Longjumps",
                        "Redesigned Speed (OldNCP)",
                        "Redesigned AutoBlock",
                        "Removed Herobrine"));

        buttons.add(new Button("Version 1.7", 5, y+=20, 75, 15,
                "Added New Emotes",
                        "Added New MainMenu",
                        "Fixed Some Bugs",
                        "Redesigned KillAura (Switch)",
                        "Redesigned Whole Client",
                        "Redesigned IRC",
                        "Removed Axis"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        ScaledResolution sr = RenderUtils.SCALED_RES;
        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiMainMenu.DEFAULT_MENU);
        GlStateManager.color(1,1,1,1);
        int m = 2;
        int texWidth = sr.getScaledWidth()*m;
        int texHeight = sr.getScaledHeight()*m;
        drawModalRectWithCustomSizedTexture(mouseX-texWidth/2, mouseY-texHeight/2, 0, 0, texWidth, texHeight, texWidth, texHeight);
        Gui.drawRect(0,0,sr.getScaledWidth(),sr.getScaledHeight(),new Color(1,1,1,120).getRGB());
        for (Button button : buttons) {
            button.render(mouseX,mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (Button button : buttons) {
            if (button.hovered(mouseX,mouseY)) {
                selected = button;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }


    public class Button {
        private String name;
        private String[] texts;
        private double x,y,width,height;

        public Button(String name, double x, double y, double width, double height, String... texts) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.texts = texts;
        }

        public boolean hovered(int mouseX,int mouseY) {
            return mouseX>=x && mouseX<=x+width && mouseY >= y && mouseY<=y+height;
        }
        public void render(int mouseX,int mouseY) {
            drawRect(x,y,x+width,y+height, Color.red.getRGB());
            if (hovered(mouseX,mouseY)) {
                drawRect(x,y,x+width,y+height, Color.CYAN.getRGB());
            }
            if (selected == this) {
                drawRect(x,y,x+width,y+height, new Color(75,100,255,120).getRGB());
            }
            fontRendererObj.drawStringWithShadow(name, (float) (((x+width)/2)-((fontRendererObj.getStringWidth(name)-x)/2)),(float)y+3,Color.white.getRGB());
            if (selected == this) {
                double startX = x+width+10;
                double startY = 5;
                for (String splitText : getTexts()) {
                    drawColoredChangelog(splitText,startX,startY,Color.white.getRGB());
                    startY+=15;
                }
            }
        }
        public void drawColoredChangelog(String text,double x,double y,int color) {
            String za = text.replace("Added","§aAdded§f").replace("Removed","§cRemoved§f").replace("Fixed","§eFixed§f").replace("Redesigned","§bRedesigned§f").replace("Optimized"," §9Optimized§f").replace("Fucked"," §4Fucked§f");
            fontRendererObj.drawStringWithShadow(za,(float)x,(float)y,color);
        }
        public double getX() {
            return x;
        }

        public void setX(double x) {
            this.x = x;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        public double getWidth() {
            return width;
        }

        public void setWidth(double width) {
            this.width = width;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String[] getTexts() {
            return texts;
        }
    }
}
