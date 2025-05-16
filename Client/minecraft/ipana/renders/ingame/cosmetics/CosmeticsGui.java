package ipana.renders.ingame.cosmetics;

import ipana.Ipana;

import static ipana.irc.user.PlayerCosmetics.*;
import static ipana.irc.user.PlayerCosmetics.CapeType.*;

import ipana.irc.user.PlayerCosmetics;
import ipana.irc.user.User;
import ipana.renders.ingame.EmoteGui;
import ipana.utils.player.PlayerUtils;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CosmeticsGui extends GuiScreen {
    private CameraScene DEFAULT = new CameraScene(0,100,100, 0,0);
    private static final Color HOVER = new Color(125,125,125,125);
    private static final Color BACKGROUND = new Color(175,175,175,125);
    private static final Color COLOR = new Color(125,125,255,125);
    CosmeticRenders renders;
    private CameraScene scene = DEFAULT;
    public GuiTextField capeText;
    public boolean renderArmor;
    public boolean physicsCape;
    private List<Box> boxes;
    private ColorPicker picker = new ColorPicker((ColorPicker doinb) -> {
        Ipana.mainIRC().self().cosmetics().setCosmetics(EARS, doinb.currentValue);
        Ipana.mainIRC().self().cosmetics().sendPacket();
    }, savedColor);
    public static int savedColor;
    private int lastMouseX , lastMouseY;

    public CosmeticsGui() {
        picker.currentValue = savedColor;
        ScaledResolution sr = RenderUtils.SCALED_RES;
        int x = sr.getScaledWidth()/2;
        int y = sr.getScaledHeight()/2;
        capeText = new GuiTextField(1, Minecraft.getMinecraft().fontRendererObj, sr.getScaledWidth()-195, sr.getScaledHeight()-20, 190, 15);
        capeText.setMaxStringLength(1000000);
        User self = Ipana.mainIRC().self();
        PlayerCosmetics cosmetics = self.cosmetics();
        int MODEL = MODELS;
        boxes = new ArrayList<>();
        renders = new CosmeticRenders(this);

        boxes.add(new Box("Head", x - 80, 5, 50, 12, new CameraScene(0, 600, 300, 45, 15), new Tab("Ears", a -> {
            if (cosmetics.doesPlayerHave(EARS)) {
                cosmetics.maskID -= EARS;
            }
            cosmetics.setCosmetics(EARS, -2173);
            Ipana.mainIRC().self().cosmetics().sendPacket();
        }, new Element("Rainbow", onDraw -> renders.drawWithEars(onDraw, 2173), onClick -> {
            if (!cosmetics.doesPlayerHave(EARS)) {
                cosmetics.maskID += EARS;
            }
            cosmetics.setCosmetics(EARS, 2173);
            Ipana.mainIRC().self().cosmetics().sendPacket();
        }), new Element("Color", onDraw -> renders.drawWithEars(onDraw, savedColor), onClick -> {
            if (!cosmetics.doesPlayerHave(EARS)) {
                cosmetics.maskID += EARS;
            }
            cosmetics.setCosmetics(EARS, savedColor);
            Ipana.mainIRC().self().cosmetics().sendPacket();
        }))));

        boxes.add(new Box("Body",x+30,5,50,12,new CameraScene(0,400,300, 225, 15),
                new Tab("Cape", a -> {
                    if (cosmetics.doesPlayerHave(CAPE)) {
                        cosmetics.maskID -= CAPE;
                    }
                    cosmetics.setCosmetics(CAPE, "none","none", PlayerCosmetics.CapeType.EMPTY, new ResourceLocation(""));
                    Ipana.mainIRC().self().cosmetics().sendPacket();
                }, capes(cosmetics))));

        boxes.add(new Box("Player",x-25,5,50,12,new CameraScene(0,200,200, 45, 15),
                new Tab("Models", a -> {
                    if (cosmetics.doesPlayerHave(MODEL)) {
                        cosmetics.maskID -= MODEL;
                    }
                    cosmetics.setCosmetics(PlayerCosmetics.MODELS, "none", new ResourceLocation("mesir/models/holycat.jpg"));
                    Ipana.mainIRC().self().cosmetics().sendPacket();
                }, new Element("Normal Among Us", a -> renders.drawAmongus(false), a -> {
                    if (!cosmetics.doesPlayerHave(MODEL)) {
                        cosmetics.maskID += MODEL;
                    }
                    cosmetics.setCosmetics(PlayerCosmetics.MODELS, "normal_amongus",new ResourceLocation("mesir/models/normal_amongus.png"));
                    Ipana.mainIRC().self().cosmetics().sendPacket();
                }), new Element("LGBT Among Us", a -> renders.drawAmongus(true), a -> {
                    if (!cosmetics.doesPlayerHave(MODEL)) {
                        cosmetics.maskID += MODEL;
                    }
                    cosmetics.setCosmetics(PlayerCosmetics.MODELS, "lgbt_amongus",new ResourceLocation("mesir/models/lgbt_amongus.png"));
                    Ipana.mainIRC().self().cosmetics().sendPacket();
                }), new Element("Ela", a -> renders.drawEla(), a -> {
                    if (!cosmetics.doesPlayerHave(MODEL)) {
                        cosmetics.maskID += MODEL;
                    }
                    cosmetics.setCosmetics(PlayerCosmetics.MODELS, "ela",new ResourceLocation("mesir/models/ela.png"));
                    Ipana.mainIRC().self().cosmetics().sendPacket();
                }))));
    }

    @Override
    public void initGui() {
        buttonList.add(new GuiButton(0, 5,5, 100, 20,"Render Armor"));
        buttonList.add(new GuiButton(1, 5,30, 100, 20,"Physics Cape"));
        buttonList.add(new GuiSlider(2, 5,55, 100, 20,"Scale", 0.25f, 2f, (Float) Ipana.mainIRC().self().cosmetics().getCosmetic(CHILD).params()[0]));
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = RenderUtils.SCALED_RES;
        RenderUtils.drawFixedRectWH(0,0,sr.getScaledWidth(),sr.getScaledHeight(),BACKGROUND);
        int x = sr.getScaledWidth()/2+scene.x;
        int y = sr.getScaledHeight()/2+scene.y;
        renders.drawEntityOnScreen(x,y,scene.scale,scene.yaw,scene.pitch,mc.thePlayer);

        if (scene != DEFAULT) {
            RenderUtils.drawFixedRectWH(sr.getScaledWidth()-200,0,200,sr.getScaledHeight(),HOVER);
        }
        for (Box box : boxes) {
            box.draw(mouseX, mouseY);
            if ("Body".equals(box.name) && box.cameraScene == scene) {
                capeText.drawTextBox();
            }
            if ("Head".equals(box.name) && box.cameraScene == scene) {
                picker.draw(sr.getScaledWidth() - 190, sr.getScaledHeight() - 120, 100, 100, mouseX, mouseY, new Color(savedColor));
                if (picker.currentValue != savedColor) {
                    Ipana.mainIRC().self().cosmetics().setCosmetics(EARS, savedColor);
                    savedColor = picker.currentValue;
                }
            }
        }
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (((GuiSlider)buttonList.get(2)).isMouseDown) {
            Ipana.mainIRC().self().cosmetics().setCosmetics(CHILD, ((GuiSlider)buttonList.get(2)).func_175220_c());
        }
    }


    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (Box box : boxes) {
            box.mouseClicked(mouseX, mouseY);
        }
        capeText.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 0) {
            renderArmor ^= true;
        }
        if (button.id == 1) {
            physicsCape ^= true;
        }
        super.actionPerformed(button);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (((GuiSlider)buttonList.get(2)).isMouseDown) {
            Ipana.mainIRC().self().cosmetics().setCosmetics(CHILD, ((GuiSlider)buttonList.get(2)).func_175220_c());
            Ipana.mainIRC().self().cosmetics().sendPacket();
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        capeText.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (scene != DEFAULT) {
                scene = DEFAULT;
            } else {
                //Eymen burda null pointer yerse kafasını kesicem
                mc.displayGuiScreen(Ipana.emoteGUI);
            }
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }

    private ArrayList<Element> capes(PlayerCosmetics cosmetics) {
        ArrayList<Element> elements = new ArrayList<>();
        String[] localCapes = new String[] {"Ruby", "Ipana","Ataturk","Reborn","Biqsemoq","Ipana1.3","Mentos"};
        String[] shaderCapes = new String[] {"Cat"};
        for (String cape : localCapes) {
            elements.add(new Element(cape, a -> renders.drawWithCape(a, "none", PlayerCosmetics.CapeType.LOCAL), a -> {
                if (!cosmetics.doesPlayerHave(CAPE)) {
                    cosmetics.maskID += CAPE;
                }
                setCape(a, "none", PlayerCosmetics.CapeType.LOCAL);
                Ipana.mainIRC().self().cosmetics().sendPacket();
            }));
        }
        for (String cape : shaderCapes) {
            elements.add(new Element(cape, a -> renders.drawWithCape(a, "fade_cape", SHADERS), a -> {
                if (!cosmetics.doesPlayerHave(CAPE)) {
                    cosmetics.maskID += CAPE;
                }
                setCape(a, "fade_cape", SHADERS);
                Ipana.mainIRC().self().cosmetics().sendPacket();
            }));
        }
        elements.add(new Element("Custom", a -> renders.drawWithCape(a, "none", URL), a -> {
            if (!cosmetics.doesPlayerHave(CAPE)) {
                cosmetics.maskID += CAPE;
            }
            setCape(a, "none", URL);
            Ipana.mainIRC().self().cosmetics().sendPacket();
        }));
        return elements;
    }

    class Box {
        String name;
        int x,y,width,height;
        CameraScene cameraScene;
        List<Tab> tabs;


        public Box(String name, int x, int y, int width, int height, CameraScene scene, Tab... tab) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.cameraScene = scene;
            tabs = new ArrayList<>();
            Collections.addAll(tabs, tab);
        }

        void draw(int mouseX, int mouseY) {
            if (scene == DEFAULT) {
                if (hovered(mouseX, mouseY)) {
                    RenderUtils.drawFixedRectWH(x, y, width, height, HOVER);
                }
                RenderUtils.drawFixedRectWH(x, y, width, height, COLOR);
                GlStateManager.pushMatrix();
                GlStateManager.disableDepth();
                mc.fontRendererObj.drawStringWithShadow(name, x + (width/2f-mc.fontRendererObj.getStringWidth(name)/2f), y + (height / 2f - mc.fontRendererObj.FONT_HEIGHT / 2f), Color.white.getRGB());
                GlStateManager.popMatrix();
            } else if (scene == cameraScene) {
                int renderX = RenderUtils.SCALED_RES.getScaledWidth()-195;
                int renderY = 5;
                for (Tab tab : tabs) {
                    tab.draw(renderX, renderY, mouseX, mouseY);
                    renderY+=12;
                }
            }
        }

        void mouseClicked(int mouseX, int mouseY) {
            if (scene == DEFAULT) {
                if (hovered(mouseX, mouseY)) {
                    scene = cameraScene;
                }
            } else if (scene == cameraScene) {
                int renderY = 5;
                for (Tab tab : tabs) {
                    tab.mouseClicked(renderY, mouseX, mouseY);
                    renderY+=12;
                }
            }
        }

        boolean hovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x+width && mouseY >= y && mouseY <= y+height;
        }
    }

    class CameraScene {
        int x,y,scale,yaw,pitch;

        public CameraScene(int x, int y, int scale, int yaw, int pitch) {
            this.x = x;
            this.y = y;
            this.scale = scale;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    private void setCape(Element element, String shaderName, PlayerCosmetics.CapeType type) {
        User self = Ipana.mainIRC().self();
        PlayerCosmetics cosmetics = self.cosmetics();
        String capeShit = "Custom".equals(element.elementName) ? capeText.getText() : element.elementName;
        cosmetics.setCosmetics(CAPE, capeShit, shaderName, type, cosmetics.parseCape(capeShit));
    }
}
