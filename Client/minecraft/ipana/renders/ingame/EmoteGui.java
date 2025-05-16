package ipana.renders.ingame;

import ipana.Ipana;
import ipana.irc.IRC;
import ipana.irc.user.UsersGui;
import ipana.renders.ingame.cosmetics.CosmeticsGui;
import ipana.utils.font.FontHelper;
import ipana.utils.font.FontUtil;
import ipana.utils.render.EmoteUtils;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import stelixobject.objectfile.SxfDataObject;
import stelixobject.objectfile.SxfFile;
import stelixobject.objectfile.reader.SXfReader;
import stelixobject.objectfile.writer.SxfWriter;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ipana.utils.config.ConfigUtils.getConfigFile;

public class EmoteGui extends GuiScreen {
    private final Color background = new Color(255,255,255,150);
    private final Color hexHovered = new Color(0, 140, 255,75);
    private List<EmoteUtils.Emote> available = new ArrayList<>();
    private List<EmoteUtils.Emote> using = new ArrayList<>();
    private List<Hexagon> hexagons = new ArrayList<>();
    private FontUtil font = FontHelper.SIZE_18;
    private EmoteUtils.Emote selectedEmote;
    private int displayList;
    public float yaw,pitch;


    public EmoteGui() {
        super();
        int hexagonSize = 60;
        int x = RenderUtils.SCALED_RES.getScaledWidth()/2-hexagonSize+hexagonSize/4;
        int y = RenderUtils.SCALED_RES.getScaledHeight()/2-hexagonSize-hexagonSize/4;
        hexagons.add(new Hexagon(x,y,hexagonSize,hexagonSize));
        x+=hexagonSize-hexagonSize/4;
        hexagons.add(new Hexagon(x,y,hexagonSize,hexagonSize));
        x+=hexagonSize/2;
        y+=hexagonSize/2;
        hexagons.add(new Hexagon(x,y,hexagonSize,hexagonSize));
        y+=hexagonSize-hexagonSize/4;
        hexagons.add(new Hexagon(x,y,hexagonSize,hexagonSize));
        y+=hexagonSize/2;
        x-=hexagonSize/2;
        hexagons.add(new Hexagon(x,y,hexagonSize,hexagonSize));
        x-=hexagonSize-hexagonSize/4;
        hexagons.add(new Hexagon(x,y,hexagonSize,hexagonSize));
        x-=hexagonSize/2;
        y-=hexagonSize/2;
        hexagons.add(new Hexagon(x,y,hexagonSize,hexagonSize));
        y-=hexagonSize-hexagonSize/4;
        hexagons.add(new Hexagon(x,y,hexagonSize,hexagonSize));
        displayList = -1;
        load();
        updateAvailable();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            yaw = mc.thePlayer.rotationYaw;
            pitch = mc.thePlayer.rotationPitch;
        }

    }

    @Override
    public void initGui() {
        buttonList.add(new GuiButton(1, RenderUtils.SCALED_RES.getScaledWidth()-100,RenderUtils.SCALED_RES.getScaledHeight()/2+20, 60, 20, "Cosmetics"));
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.thePlayer.rotationYaw = yaw;
        mc.thePlayer.rotationPitch = pitch;
        mc.thePlayer.rotationYaw += (mouseX-RenderUtils.SCALED_RES.getScaledWidth()/2f)/7;
        mc.thePlayer.rotationPitch += (mouseY-RenderUtils.SCALED_RES.getScaledHeight()/2f)/7;
        RenderUtils.drawFixedRect(0,0,RenderUtils.SCALED_RES.getScaledWidth(),RenderUtils.SCALED_RES.getScaledHeight(),background);
        drawAllHexagons();
        for (Hexagon hexagon : hexagons) {
            hexagon.drawHovered(mouseX, mouseY);
        }

        int x = 5;
        int y = 5;
        for (EmoteUtils.Emote emote : available) {
            if (mouseX >= x-1 && mouseY >= y-1 && mouseX <= x+font.getWidth(emote.getName())+12 && mouseY <= y+13) {
                RenderUtils.drawFixedRect(x-1,y-1,x+font.getWidth(emote.getName())+12,y+13,Color.lightGray);
            }
            if (emote == selectedEmote) {
                RenderUtils.drawFixedRect(x-1,y-1,x+font.getWidth(emote.getName())+12,y+13,Color.blue.brighter());
            }
            emote.render(x, y, 12,12);
            font.drawStringWithShadow(emote.getName(), x+12, y+2,Color.white);
            y+=15;
        }

        if (selectedEmote != null) {
            selectedEmote.render(mouseX, mouseY, 12,12);
        }
        // COSMETICS
        drawEntityOnScreen(RenderUtils.SCALED_RES.getScaledWidth()-70, RenderUtils.SCALED_RES.getScaledHeight()/2+10, 40, 180, 0,mc.thePlayer);
        Ipana.usersGui().drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        int x = 5;
        int y = 5;
        Ipana.usersGui().onMouseClicked(mouseX, mouseY, mouseButton);
        for (EmoteUtils.Emote emote : available) {
            if (mouseX >= x-1 && mouseY >= y-1 && mouseX <= x+font.getWidth(emote.getName())+12 && mouseY <= y+13) {
                selectedEmote = emote;
            }
            y+=15;
        }
        for (Hexagon hexagon : hexagons) {
            if (hexagon.isHovered(mouseX, mouseY)) {
                if (mouseButton == 0) {
                    if (hexagon.emote != null) {
                        mc.thePlayer.sendChatMessage(":" + hexagon.emote.getName() + ":");
                        save();
                        mc.displayGuiScreen(null);
                    }
                } else if (mouseButton == 1) {
                    using.remove(hexagon.emote);
                    updateAvailable();
                    hexagon.emote = null;
                }
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            if (Ipana.cosmeticsGUI == null) {
                Ipana.cosmeticsGUI = new CosmeticsGui();
            }
            mc.displayGuiScreen(Ipana.cosmeticsGUI);
        }
        super.actionPerformed(button);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        Ipana.usersGui().onMouseRelease(mouseX, mouseY, state);
        if (selectedEmote != null) {
            for (Hexagon hexagon : hexagons) {
                if (hexagon.isHovered(mouseX, mouseY)) {
                    if (hexagon.emote == null) {
                        hexagon.emote = selectedEmote;
                        using.add(selectedEmote);
                    } else {
                        using.set(using.indexOf(hexagon.emote), selectedEmote);
                        hexagon.emote = selectedEmote;
                    }
                    updateAvailable();
                }
            }
            selectedEmote = null;
        }
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            save();
        }
        super.keyTyped(typedChar, keyCode);
    }

    private void drawAllHexagons() {
        if (displayList == -1) {
            displayList = GLAllocation.generateDisplayLists(1);
        }
        GL11.glNewList(displayList, GL11.GL_COMPILE);
        for (Hexagon hexagon : hexagons) {
            hexagon.drawHexagon();
        }
        GL11.glEndList();
        GL11.glCallList(displayList);
    }

    private void updateAvailable() {
        available.clear();
        for (EmoteUtils.Emote emote : EmoteUtils.getList()) {
            if (!using.contains(emote)) {
                available.add(emote);
            }
        }
    }

    private void save() {
        SxfFile file = new SxfFile();
        SxfDataObject data = new SxfDataObject();
        for (int i = 0; i < hexagons.size(); i++) {
            Hexagon hexagon = hexagons.get(i);
            if (hexagon.emote != null) {
                data.variables().put("hex"+i, hexagon.emote.getName());
            } else {
                data.variables().put("hex"+i, "null");
            }
        }
        file.base().put("Emotes",data);
        new SxfWriter(file).write(getConfigFile("Emotes.sxf").getAbsolutePath());
    }

    public void load() {
        SxfFile file = SXfReader.Read(getConfigFile("Emotes.sxf").getAbsolutePath());
        if (file.base().size() == 0)
            return;

        for (Map.Entry<String, Object> entry : file.get("Emotes").variables().entrySet()) {
            EmoteUtils.Emote emote = EmoteUtils.getEmote(String.valueOf(entry.getValue()));
            hexagons.get(Integer.parseInt(entry.getKey().substring(3))).emote = emote;
            using.add(emote);
        }
    }

    public static void drawEntityOnScreen(int posX, int posY, int scale, float mouseX, float mouseY, EntityLivingBase ent) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float)posX, (float)posY, 50.0F);
        GlStateManager.scale((float)(-scale), (float)scale, (float)scale);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        float f = ent.renderYawOffset;
        float f1 = ent.rotationYaw;
        float f2 = ent.rotationPitch;
        float f3 = ent.prevRotationYawHead;
        float f4 = ent.rotationYawHead;
        float f5 = ent.rotationPitchHead;
        float f6 = ent.prevRotationPitchHead;
        GlStateManager.rotate(135.0F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        //GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
        //GlStateManager.rotate(-((float)Math.atan((double)(mouseY / 40.0F))) * 20.0F, 1.0F, 0.0F, 0.0F);
        ent.renderYawOffset = mouseX;
        ent.rotationYaw = mouseX;
        ent.rotationPitch = mouseY;
        ent.rotationPitchHead = mouseY;
        ent.rotationYawHead = ent.rotationYaw;
        ent.prevRotationPitchHead = ent.rotationPitchHead;
        ent.prevRotationYawHead = ent.rotationYaw;
        GlStateManager.translate(0.0F, 0.0F, 0.0F);
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        rendermanager.setPlayerViewY(180.0F);
        rendermanager.setRenderShadow(false);
        rendermanager.renderEntityWithPosYaw(ent, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rendermanager.setRenderShadow(true);
        ent.renderYawOffset = f;
        ent.rotationYaw = f1;
        ent.rotationPitch = f2;
        ent.prevRotationYawHead = f3;
        ent.rotationYawHead = f4;
        ent.rotationPitchHead = f5;
        ent.prevRotationPitchHead = f6;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }


    class Hexagon {
        int x,y,width,height;
        EmoteUtils.Emote emote;

        Hexagon(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        void drawHexagon() {
            RenderUtils.drawHexagon(x,y, width,height,5, Color.black);
        }

        void drawHovered(int mouseX, int mouseY) {
            if (emote != null) {
                emote.render(x+width/8f, y+height/8f,width/2f,height/2f);
            }
            if (isHovered(mouseX, mouseY)) {
                RenderUtils.drawFixedRect(x+width/8f, y+height/8f, x+width-width/4f-width/8f, y+height-height/4f-height/8f, hexHovered);
            }
        }

        boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x+width/8 && mouseY >= y+height/8 && mouseX <= x+width-width/4-width/8 && mouseY <= y+height-height/4-height/8;
        }
    }
}
