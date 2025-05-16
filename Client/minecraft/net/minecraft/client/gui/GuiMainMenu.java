package net.minecraft.client.gui;

import ipana.Ipana;
import ipana.irc.IRC;
import ipana.irc.user.UsersGui;
import ipana.renders.account.FakeGoBrr;
import ipana.renders.account.Fakekekke;
import ipana.renders.account.WorldButFake;
import ipana.utils.font.FontHelper;
import ipana.utils.font.FontUtil;
import ipana.utils.math.MathUtils;
import ipana.utils.music.Music;
import ipana.utils.music.MusicPlayer;
import ipana.utils.music.Musics;
import ipana.utils.music.PlayLocation;
import ipana.utils.render.EndPortalRenderer;
import ipana.utils.render.RenderUtils;
import ipana.utils.shader.ShaderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GuiMainMenu extends GuiScreen{
    public static final ResourceLocation DEFAULT_MENU = new ResourceLocation("mesir/newmain/default.png");
    public static final ResourceLocation PORTAL = new ResourceLocation("mesir/newmain/portal.png");
    private static Color backgroundColor = null;
    private static final Color portalColor = new Color(5,5,5,100);
    private static final Color hoveredColor = new Color(0, 141, 239,175);
    private static final Color zero = new Color(0, 0, 0,0);

    private FontUtil font;

    private Button singlePlayer = new Button("Single Player", Button.Alignment.MIDDLE, button -> mc.displayGuiScreen(new GuiSelectWorld(this)));
    private Button multiPlayer = new Button("Multi Player", Button.Alignment.MIDDLE, button -> mc.displayGuiScreen(new GuiMultiplayer(this)));
    private Button options = new Button("Options", Button.Alignment.MIDDLE, button -> mc.displayGuiScreen(new GuiOptions(this, mc.gameSettings)));
    private Button quit = new Button("Quit", Button.Alignment.MIDDLE, button -> mc.shutdown());
    private Button accounts = new Button("Accounts", Button.Alignment.MIDDLE_RIGHT, button -> mc.displayGuiScreen(Ipana.accountManager));
    private Button changelog = new Button("Changelogs", Button.Alignment.BOTTOM_LEFT, button -> mc.displayGuiScreen(Ipana.changelogUI));
    private EndPortalRenderer portalRenderer = new EndPortalRenderer(4, new Color(132, 0, 255, 255), new Color(0, 0 ,0, 0));
    private List<Button> buttons = new ArrayList<>();

    public static Fakekekke player;

    public GuiMainMenu() {
        buttons.add(singlePlayer);
        buttons.add(multiPlayer);
        buttons.add(options);
        buttons.add(quit);

        buttons.add(accounts);

        buttons.add(changelog);
        Minecraft mc = Minecraft.getMinecraft();
        FakeGoBrr lol = new FakeGoBrr(mc,mc.session.getProfile());
        WorldButFake world = new WorldButFake(lol, new WorldSettings(0L, WorldSettings.GameType.SURVIVAL, false, false, WorldType.DEFAULT));
        player = new Fakekekke(lol, world, mc.session.getProfile());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        font = FontHelper.SIZE_24_BOLD;
        while (font == null) {
            super.drawScreen(mouseX, mouseY, partialTicks);
        }
        if ((!MusicPlayer.isPlaying() || MusicPlayer.lastPlayed().playLocation() == PlayLocation.IN_GAME) && mc.theWorld == null && !Musics.MAIN_MENU.isEmpty()) {
            if (MusicPlayer.isPlaying()) {
                MusicPlayer.stop();
            }
            Music random = Musics.MAIN_MENU.get(MathUtils.random(0, Musics.MAIN_MENU.size()));
            //MusicPlayer.play(random);
        }
        ScaledResolution sr = RenderUtils.SCALED_RES;
        GlStateManager.pushMatrix();
        GlStateManager.enableDepth();
        drawBackground(sr, mouseX, mouseY);
        if (MusicPlayer.isPlaying()) {
            String text = "§aCurrently playing: "+MusicPlayer.lastPlayed().name();
            font.drawStringWithShadow(text, sr.getScaledWidth()/2f - font.getWidth(text)/2f, 2, Color.white);
        }
        drawMiddleButtons(sr, mouseX, mouseY, partialTicks);
        drawBottomLeft(sr, mouseX, mouseY, partialTicks);
        drawMiddleRight(sr, mouseX, mouseY, partialTicks);
        GlStateManager.popMatrix();
        Ipana.usersGui().drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        onButtonClick(mouseX, mouseY, mouseButton);
        Ipana.usersGui().onMouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        Ipana.usersGui().onMouseRelease(mouseX, mouseY, state);
        super.mouseReleased(mouseX, mouseY, state);
    }

    private void drawMiddleRight(ScaledResolution sr, int mouseX, int mouseY, float partialTicks) {
        int x = sr.getScaledWidth();
        int y = sr.getScaledHeight()/2;
        RenderUtils.drawRoundedRect(x-140, y-90,80,150,10,backgroundColor);
        accounts.set(x-100, y+40, accounts.calcWidth(), 14);
        accounts.draw(mouseX, mouseY, partialTicks);
        if (player != null) {
            GuiInventory.drawEntityOnScreen(x - 100, y + 30, 60, -mouseX + x - 100, -mouseY + y, player);
        }
    }

    private void drawBottomLeft(ScaledResolution sr, int mouseX, int mouseY, float partialTicks) {
        int x = 2;
        int y = sr.getScaledHeight()-42;
        font.drawStringWithShadow("Current Build: b"+Ipana.version, x, y, Color.white);
        font.drawStringWithShadow("Running on: "+System.getProperty("java.version"), x, y+14, Color.white);
        font.drawStringWithShadow("Click here to see older builds.", x, y+28, Color.white);
        changelog.set(x-1, y-1, 150, sr.getScaledHeight());
    }

    private void drawMiddleButtons(ScaledResolution sr, int mouseX, int mouseY, float partialTicks) {
        List<Button> middleButtons = buttons.stream().filter(b -> b.alignment == Button.Alignment.MIDDLE).collect(Collectors.toList());
        float yOffset = 15;
        float startX = sr.getScaledWidth()/2f;
        float startY = sr.getScaledHeight()/2f-(middleButtons.size()*yOffset)/2;
        float startY2 = sr.getScaledHeight()/2f-(middleButtons.size()*yOffset)/2;
        float maxWidth = 0;
        float maxHeight = 0;
        for (Button button : middleButtons) {
            float buttonWidth = font.getWidth(button.name);
            float x = startX-buttonWidth/2;
            float y = startY;
            double distMouse = distToMouse(startX, y+yOffset/2f, mouseX, mouseY);
            GlStateManager.pushMatrix();
            GlStateManager.translate(startX,y,1);
            if (button.hovered(mouseX, mouseY)) {
                button.upScale(1.5f);
                float scaleFactor = button.prevScale + (button.scale - button.prevScale) * partialTicks;
                GlStateManager.scale(scaleFactor, scaleFactor,1);

                RenderUtils.drawRoundedGradientRect((int)(-buttonWidth/2-3),-5, (int)(buttonWidth)+6, (int)yOffset+6, 10, 10, hoveredColor, backgroundColor);
                //RenderUtils.drawRoundedRect(-buttonWidth/2,-2,buttonWidth,yOffset,5, hoveredColor);
                distMouse = 0;
                maxHeight = yOffset*button.scale+15;
            } else {
                button.downScale();
            }
            font.drawStringWithShadow(button.name, -buttonWidth/2, 0, new Color(Math.max(50, 200-(int)(distMouse*2.5)),0,Math.max(75, 255-(int)(distMouse*2.5))));
            GlStateManager.popMatrix();
            button.set(x,y,buttonWidth*button.scale, yOffset*button.scale);
            if (maxWidth < button.width+22) {
                maxWidth = button.width+22;
            }

            startY += yOffset*button.scale;
        }
        middleButtons.clear();
        RenderUtils.drawRoundedRect(startX-1-maxWidth/2f, startY2-2-maxHeight/2, maxWidth+4,startY-startY2+2+maxHeight,10,backgroundColor);
        FontUtil eras = FontHelper.eras;
        String watermark = Ipana.clientName;
        float width = eras.getWidth(watermark);
        float offset = 1f;
        float posY = startY2-2-maxHeight/2-50;
        if (mouseX > startX-1-maxWidth/2f && mouseX < startX-1-maxWidth/2f+maxWidth+4 && mouseY > startY2-maxHeight/2 && mouseY < startY2-2-maxHeight/2+startY-startY2+2+maxHeight && posY == startY2-52) {
            posY-=19;
        }
        eras.drawString(watermark, sr.getScaledWidth()/2f-width/2-offset,posY-offset, Color.darkGray);
        if (ms == -1) {
            ms = System.currentTimeMillis();
        }
        ShaderManager manager = ShaderManager.getInstance();
        manager.loadShader("make_gold");
        manager.loadData("make_gold","amount", (ms - System.currentTimeMillis()) / 2000f);
        manager.loadData("make_gold", "offset", 5f);
        eras.drawString(watermark, sr.getScaledWidth()/2f-width/2,posY, Color.white);
        manager.stop("make_gold");
        eras.drawString(watermark, sr.getScaledWidth()/2f-width/2+offset,posY+offset, Color.darkGray.darker().darker().darker());
    }

    private long ms = -1;

    private void onButtonClick(int mouseX, int mouseY, int mouseButton) {
        for (Button button : buttons) {
            boolean hover = button.alignment == Button.Alignment.MIDDLE ? button.hovered(mouseX, mouseY) : button.hovered2(mouseX, mouseY);
            if (hover) {
                button.onClick.accept(button);
            }
        }
    }

    private void drawBackground(ScaledResolution sr, int mouseX, int mouseY) {
        if (backgroundColor == null) {
            switch (Ipana.mainMenuTheme) {
                case SPACE -> backgroundColor = new Color(5,5,5,100);
                case PORTAL -> backgroundColor = new Color(75,75,75,100);
            }
        }
        switch (Ipana.mainMenuTheme) {
            case SPACE -> {
                Minecraft.getMinecraft().getTextureManager().bindTexture(DEFAULT_MENU);
                GlStateManager.enableAlpha();
                GlStateManager.color(1,1,1,1);
                int m = 2;
                int texWidth = sr.getScaledWidth()*m;
                int texHeight = sr.getScaledHeight()*m;
                drawModalRectWithCustomSizedTexture(mouseX-texWidth/2, mouseY-texHeight/2, 0, 0, texWidth, texHeight, texWidth, texHeight);
                Gui.drawRect(0,0, sr.getScaledWidth(), sr.getScaledHeight(), backgroundColor);
            }
            case PORTAL -> {
                Gui.drawRect(0,0, sr.getScaledWidth(), sr.getScaledHeight(), portalColor);
                portalRenderer.renderEndPortalEffect2D(0, 0, sr.getScaledWidth(), sr.getScaledHeight());
            }
        }
    }

    class Button {
        String name;
        Consumer<Button> onClick;
        float scale, prevScale;
        float x,y,width,height;
        Alignment alignment;

        public Button(String name, Alignment alignment, Consumer<Button> onClick) {
            this.name = name;
            this.onClick = onClick;
            this.alignment = alignment;
            scale = 1;
            prevScale = scale;
        }

        void draw(int mouseX, int mouseY, float partialTicks) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x,y,1);
            double distMouse = distToMouse(x, y+height/2f, mouseX, mouseY);
            if (hovered2(mouseX, mouseY)) {
                upScale(1.5f);
                float scaleFactor = prevScale + (scale - prevScale) * partialTicks;
                GlStateManager.scale(scaleFactor, scaleFactor,1);
                //RenderUtils.shadowAround(-width/2+2,-1, width-4, height-2, hoveredColor, zero);
                //RenderUtils.drawRoundedRect(-width/2,-2,width,height,5, hoveredColor);
                RenderUtils.drawRoundedGradientRect((int)(-width/2-4), -6, (int)(width+8), (int)height+8, 10, 10, hoveredColor, zero);
                distMouse = 0;
            } else {
                downScale();
            }
            font.drawStringWithShadow(name, -width/2, 0, new Color(Math.max(50, 200-(int)(distMouse*2.5)),0,Math.max(75, 255-(int)(distMouse*2.5))));
            GlStateManager.popMatrix();
        }
        boolean hovered2(int mouseX, int mouseY) {
            return mouseX > x-width*scale/2 && mouseX < x+width*scale && mouseY > y-2 && mouseY < y+height*scale;
        }
        boolean hovered(int mouseX, int mouseY) {
            return mouseX > x && mouseX < x+width && mouseY > y && mouseY < y+height;
        }
        void downScale() {
            prevScale = scale;
            if (scale > 1) {
                scale -= 0.05f;
            }
            scale = Math.max(1, scale);
        }

        void upScale(float to) {
            prevScale = scale;
            if (scale < to) {
                scale += 0.05f;
            }
            scale = Math.min(to, scale);
        }

        void set(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        float calcWidth() {
            return font.getWidth(name);
        }

        enum Alignment {
            MIDDLE, MIDDLE_RIGHT, BOTTOM_LEFT
        }
    }



    private double distToMouse(double x, double y, int mouseX, int mouseY) {
        double diffX = mouseX-x;
        double diffY = mouseY-y;
        return Math.sqrt(diffX*diffX+diffY*diffY);
    }

    public enum Theme {
        PORTAL, SPACE;

        public static final Theme[] VALUES = values();
    }

}
