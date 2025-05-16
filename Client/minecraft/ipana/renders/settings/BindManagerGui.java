package ipana.renders.settings;

import ipana.Ipana;
import ipana.managements.module.Module;
import ipana.managements.module.ModuleManager;
import ipana.managements.value.ValueManager;
import ipana.utils.config.ConfigUtils;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BindManagerGui extends GuiScreen {

    private int scroll;
    private Minecraft mc = Minecraft.getMinecraft();
    private ScaledResolution sr = RenderUtils.SCALED_RES;
    private GuiTextField searchBox = new GuiTextField(1,mc.fontRendererObj,2,5,200,20);
    private GuiTextField key = new GuiTextField(2,mc.fontRendererObj,2,5,200,20);
    private Module listeningModule;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(GuiMainMenu.DEFAULT_MENU);
        GlStateManager.color(1,1,1,1);
        int m = 2;
        int texWidth = sr.getScaledWidth()*m;
        int texHeight = sr.getScaledHeight()*m;
        drawModalRectWithCustomSizedTexture(mouseX-texWidth/2, mouseY-texHeight/2, 0, 0, texWidth, texHeight, texWidth, texHeight);
        Gui.drawRect(0,0,sr.getScaledWidth(),sr.getScaledHeight(),new Color(1,1,1,150).getRGB());
        int y = 2+scroll;
        List<Module> list = new ArrayList<>(ModuleManager.getModuleList());
        list.sort(Comparator.comparing(Module::getName));
        boolean pos = false;
        boolean neg = false;
        for (Module mod : list) {
            if (mod.getName().toLowerCase().contains(searchBox.getText().toLowerCase())) {
                if (y < 2) {
                    neg = true;
                }
                float width = mc.fontRendererObj.getStringWidth(mod.getName() + " : " + Keyboard.getKeyName(mod.getKey()));
                float x = (float) sr.getScaledWidth() / 2 - width / 2;
                if (mouseX >= x && mouseX <= x + width && mouseY >= y - 2 && mouseY <= y + 11) {
                    Gui.drawRect(x - 1, y - 2, x + width + 1, y + 11, new Color(255, 255, 255, 150).getRGB());
                }
                if (listeningModule == mod) {
                    Gui.drawRect(x - 1, y - 2, x + width + 1, y + 11, mod.visible ? new Color(255, 1, 1, 150).getRGB() : new Color(150,150,150,150).getRGB());
                }
                mc.fontRendererObj.drawStringWithShadow(mod.getName() + " : " + Keyboard.getKeyName(mod.getKey()), x, y, Color.white.getRGB());
                if (y-25 > sr.getScaledHeight()) {
                    pos = true;
                }
                y += 25;
            }
        }
        int mouse = Mouse.getDWheel()/20;
        if ((mouse > 0 && neg) || (mouse < 0 && pos)) {
            scroll+=mouse;
        }
        List<Module> widthSort = new ArrayList<>(ModuleManager.getModuleList());
        widthSort.sort(Comparator.comparingDouble(mod -> -fontRendererObj.getStringWidth(mod.getName()+" : "+Keyboard.getKeyName(mod.getKey()))));
        float width = (float) mc.fontRendererObj.getStringWidth(widthSort.get(0).getName()+" : "+ Keyboard.getKeyName(widthSort.get(0).getKey()));
        float x = (float) sr.getScaledWidth() / 2 - width / 2;
        Gui.drawRect(x-2,0,x-3,sr.getScaledHeight(),Color.red.getRGB());
        Gui.drawRect(x+width+1,0,x+width+2,sr.getScaledHeight(),Color.red.getRGB());

        searchBox.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseX>=searchBox.xPosition && mouseX <= searchBox.xPosition+searchBox.getWidth()&& mouseY>=searchBox.yPosition&& mouseY <= searchBox.yPosition+30) {
            searchBox.setFocused(true);
        }
        int y = 2;
        List<Module> list = new ArrayList<>(ModuleManager.getModuleList());
        list.sort(Comparator.comparing(Module::getName));
        for (Module mod : list) {
            if (mod.getName().toLowerCase().contains(searchBox.getText().toLowerCase())) {
                float width = mc.fontRendererObj.getStringWidth(mod.getName() + " : " + Keyboard.getKeyName(mod.getKey()));
                float x = (float) sr.getScaledWidth() / 2 - width / 2;
                if (mouseX >= x && mouseX <= x + width && mouseY >= y - 2 + scroll && mouseY <= y + 11 + scroll) {
                    if (mouseButton == 1) {
                        if (listeningModule == mod) {
                            listeningModule.setKey(Keyboard.KEY_NONE);
                            listeningModule = null;
                            key.setFocused(false);
                        }
                    } else if (mouseButton == 0) {
                        listeningModule = mod;
                        key.setFocused(true);
                    } else if (mouseButton == 2) {
                        if (listeningModule == mod) {
                            mod.visible=!mod.visible;
                        }
                    }
                }
                y += 25;
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }



    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (key.isFocused()) {
            searchBox.setFocused(false);
            key.textboxKeyTyped(typedChar,keyCode);
            key.setFocused(false);
            listeningModule.setKey(keyCode);
            listeningModule = null;
        }
        if (searchBox.isFocused()) {
            searchBox.textboxKeyTyped(typedChar,keyCode);
            scroll=0;
            if (keyCode == Keyboard.KEY_RETURN) {
                searchBox.setFocused(false);
            }
        }
        if (keyCode==Keyboard.KEY_ESCAPE) {
            ConfigUtils.saveModsAndVals();
            System.out.println("Saved Settings!");
        }
        super.keyTyped(typedChar, keyCode);
    }
}
