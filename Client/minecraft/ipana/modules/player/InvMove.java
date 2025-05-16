package ipana.modules.player;

import ipana.events.EventPreUpdate;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.renders.ide.IDEScreen;
import ipana.renders.games.snake.SnakeGUI;
import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Keyboard;

import pisi.unitedmeows.eventapi.event.listener.Listener;

public class InvMove extends Module {

    public InvMove() {
        super("InvMove", Keyboard.KEY_NONE,Category.Player,"Moving in screen.");
    }

    private Listener<EventPreUpdate> onPre = new Listener<EventPreUpdate>(event -> {
        if (Keyboard.isKeyDown(200)) {
            mc.thePlayer.rotationPitch = (mc.thePlayer.rotationPitch - 2.0F);
        }
        if (Keyboard.isKeyDown(208)) {
            mc.thePlayer.rotationPitch = (mc.thePlayer.rotationPitch + 2.0F);
        }
        if (Keyboard.isKeyDown(203)) {
            mc.thePlayer.rotationYaw = (mc.thePlayer.rotationYaw - 3.0F);
        }
        if (Keyboard.isKeyDown(205)) {
            mc.thePlayer.rotationYaw = (mc.thePlayer.rotationYaw + 3.0F);
        }
    }).filter(filter -> mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat) && !(mc.currentScreen instanceof SnakeGUI) && !(mc.currentScreen instanceof IDEScreen));

}
