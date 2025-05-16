package ipana.renders.ide;

import ipana.renders.ide.panels.code.CodePanel;
import ipana.renders.ide.panels.pack.PackagePanel;
import ipana.renders.ide.settings.SharedAttributes;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class IDEScreen extends GuiScreen {

    public static final IDEScreen INSTANCE = new IDEScreen();

    public void setupIDE() {

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        SharedAttributes.updateWindowSizes();
        PackagePanel.INSTANCE.draw(mouseX, mouseY);
        CodePanel.INSTANCE.draw(mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        PackagePanel.INSTANCE.mouseClicked(mouseX, mouseY,mouseButton);
        CodePanel.INSTANCE.mouseClicked(mouseX, mouseY,mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        PackagePanel.INSTANCE.keyPressed(keyCode,typedChar);
        CodePanel.INSTANCE.keyPressed(keyCode,typedChar);
        super.keyTyped(typedChar, keyCode);
    }
}
