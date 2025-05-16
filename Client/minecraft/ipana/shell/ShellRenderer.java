package ipana.shell;

import ipana.Ipana;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class ShellRenderer extends GuiScreen {
    private Shell shell;

    public ShellRenderer() {
        shell = Ipana.shell;
    }


    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        shell.render(mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (shell.isHovered(mouseX, mouseY))
            shell.setMoving(true);
        shell.whenClicked(mouseX, mouseY);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (shell.isHovered(mouseX, mouseY))
            shell.setMoving(true);
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        shell.onKey(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        shell.setMoving(false);
        super.mouseReleased(mouseX, mouseY, state);
    }
}
