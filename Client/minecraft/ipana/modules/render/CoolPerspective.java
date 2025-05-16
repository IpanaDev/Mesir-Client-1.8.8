package ipana.modules.render;

import ipana.events.EventCamera;
import ipana.events.EventRender3D;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.BoolValue;
import ipana.utils.player.PlayerUtils;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.glu.Project;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import static net.minecraft.client.gui.Gui.icons;

public class CoolPerspective extends Module {
    public CoolPerspective() {
        super("CoolPerspective", Keyboard.KEY_NONE,Category.Render,"Render player in first person.");
    }
    public BoolValue NEW = new BoolValue("New",this,false,"New perspective.");

    private Listener<EventCamera> onCamera = new Listener<EventCamera>(event -> {
        if (event.getType() == EventCamera.Type.PRE) {
            double[] c = PlayerUtils.calculate2(2.5, mc.thePlayer.rotationYaw - 150, 1);
            event.setX(c[0]);
            event.setZ(c[1]);
            event.setPitch(mc.thePlayer.rotationPitch);
        }
    }).filter(event -> NEW.getValue() && mc.gameSettings.thirdPersonView == 1);

    private Listener<EventRender3D> onRender = new Listener<EventRender3D>(event -> {
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        Project.gluPerspective(mc.entityRenderer.getFOVModifier(event.partialTicks(), false), (float)(this.mc.displayWidth) / (float)(this.mc.displayHeight), 0F, 128f);
        GlStateManager.matrixMode(5888);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        //this.mc.getTextureManager().bindTexture(icons);
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY-40, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(+60,-60,120);
        GlStateManager.translate(0,-mc.thePlayer.rotationPitch/2,0);
        float scale = 0.6f;
        GlStateManager.scale(-scale,-scale,-scale);

        mc.getRenderItem().zLevel = -240.0F;
        GlStateManager.translate(0,mc.thePlayer.rotationPitch/2.5,0);

        GlStateManager.disableDepth();
        mc.ingameGUI.renderTooltip(0, 30,event.partialTicks());
        GlStateManager.translate(0,-mc.thePlayer.rotationPitch/2.5,0);
        mc.getRenderItem().zLevel = -50.0F;
        GlStateManager.scale(0.7,0.7,0.7);
        mc.getTextureManager().bindTexture(icons);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        if (this.mc.playerController.shouldDrawHUD()) {
            mc.ingameGUI.renderPlayerStats(45,0);
        }
        if (this.mc.thePlayer.isRidingHorse()) {
            mc.ingameGUI.renderHorseJumpBar(-45, -30);
        } else if (this.mc.playerController.gameIsSurvivalOrAdventure()) {
            mc.ingameGUI.renderExpBar(-45, -30);
        }
        GlStateManager.matrixMode(5889);
        GlStateManager.loadIdentity();
        Project.gluPerspective(mc.entityRenderer.getFOVModifier(event.partialTicks(), true), (float)(this.mc.displayWidth) / (float)(this.mc.displayHeight), 0.05F, mc.entityRenderer.clipDistance);
        GlStateManager.matrixMode(5888);
        GlStateManager.popMatrix();
    }).filter(event -> NEW.getValue() && mc.gameSettings.thirdPersonView == 1);
}
