package ipana.clickgui.autistic;

import ipana.Ipana;
import ipana.clickgui.autistic.panels.CategoryPanel;
import ipana.managements.module.Category;
import ipana.managements.module.ModuleManager;
import ipana.modules.render.CGui;
import ipana.utils.config.ConfigUtils;
import ipana.utils.render.RenderUtils;
import ipana.utils.shader.ShaderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.vector.Vector3f;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NewClickGui extends GuiScreen {

    public List<CategoryPanel> categoryPanels = new ArrayList<>();
    public GuiTextField textField;


    public NewClickGui() {
        GuiSettings.initFont();
        int x = 10;
        for (Category category : Category.VALUES) {
            categoryPanels.add(new CategoryPanel(x,10,category));
            x+=110;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        RenderUtils.drawGradientRect(0,0,RenderUtils.SCALED_RES.getScaledWidth(),RenderUtils.SCALED_RES.getScaledHeight(),-1072689136, Ipana.getClientColor().getRGB());
        fontRendererObj.drawString(String.valueOf(Minecraft.getDebugFPS()), 2, 2, Color.lightGray.getRGB());
        for (CategoryPanel categoryPanel : categoryPanels) {
            GuiSettings.updateColors(categoryPanel);
            categoryPanel.render(mouseX, mouseY, partialTicks);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (int i = 0; i < categoryPanels.size(); i++) {
            CategoryPanel categoryPanel = categoryPanels.get(i);
            if (categoryPanel.isHovered(mouseX, mouseY)) {
                CategoryPanel lastElement = categoryPanels.getLast();
                categoryPanels.set(categoryPanels.size()-1, categoryPanel);
                categoryPanels.set(i, lastElement);
            }
        }
        for (CategoryPanel categoryPanel : categoryPanels) {
            categoryPanel.mouseClicked(mouseX, mouseY, mouseButton);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (textField != null) {
            if (textField.isFocused()) {
                textField.textboxKeyTyped(typedChar, keyCode);
                if (keyCode == Keyboard.KEY_RETURN) {
                    textField.setFocused(false);
                }
            }
        }
        for (CategoryPanel categoryPanel : categoryPanels) {
            categoryPanel.keyTyped(typedChar, keyCode);
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            ConfigUtils.saveModsAndVals();
            ConfigUtils.saveFriends();
            CGui.saveNew();
        }
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (CategoryPanel categoryPanel : categoryPanels) {
            categoryPanel.mouseReleased(mouseX, mouseY, state);
        }
        CGui.saveNew();
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
