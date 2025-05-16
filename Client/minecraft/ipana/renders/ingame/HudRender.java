package ipana.renders.ingame;

import ipana.managements.module.Module;
import ipana.managements.module.ModuleManager;
import ipana.managements.module.Modules;
import ipana.modules.render.Hud;
import ipana.renders.settings.SettingsGui;
import ipana.utils.gamepad.GamePadRender;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.util.ResourceLocation;

public class HudRender extends GuiIngame {

    public HudRender(Minecraft mcIn) {
        super(mcIn);
    }

    private static Minecraft mc = Minecraft.getMinecraft();
    public static final HudRender instance = new HudRender(mc);

    public float voiceY;
    private Hud.Mode lastMode;


    @Override
    public void renderGameOverlay(float partialTicks) {
        super.renderGameOverlay(partialTicks);
        if (mc.currentScreen != null || mc.gameSettings.showDebugInfo) {
            return;
        }

        //Ipana.mainIRC().ircChat().inGameRender(partialTicks);
        GamePadRender.render(0,25);
        Hud hud = Modules.HUD;
        if (lastMode != hud.mode.getValue()) {
            for (Module mod : ModuleManager.getModuleList()) {
                mod.onSuffixChange();
            }
            lastMode = hud.mode.getValue();
        }
        hud.drawHUD();
        if (SettingsGui.drawGui != null) {
            SettingsGui.drawGui.renderDrawGui();
        }
    }
}
