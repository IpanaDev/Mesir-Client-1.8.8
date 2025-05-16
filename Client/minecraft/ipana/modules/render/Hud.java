package ipana.modules.render;

import ipana.Ipana;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.ModuleManager;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.EnumValue;
import ipana.managements.value.values.TextValue;
import ipana.utils.StringUtil;
import ipana.utils.font.FontHelper;
import ipana.utils.font.FontUtil;
import ipana.utils.gl.DisplayLists;
import ipana.utils.gl.GLCall;
import ipana.utils.gl.GList;
import ipana.utils.gl.GLists;
import ipana.utils.net.Pinger;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumWorldBlockLayer;
import optifine.Config;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.Comparator;

public class Hud extends Module {
    public EnumValue<Mode> mode = new EnumValue<>("Mode",this,Mode.class,"Hud mode.");
    private TextValue text = new TextValue("ClientName",this,"Ipana","Client name.");
    public BoolValue debug = new BoolValue("Debug",this,false,"Hardware debug.");
    public BoolValue potion = new BoolValue("Potion",this,true,"Draw potion effects.");
    private FontUtil font = FontHelper.SIZE_18;
    public int[] LAYERS = new int[EnumWorldBlockLayer.VALUES.length];
    public static long DEBUG_MS;
    private GList<Long> debugLong = new GList<>();
    private GLists<Integer> globalRenderCalls = new GLists<>(2);
    private GLists<Integer> globalLists = new GLists<>(2);
    private GLists<PotionEffect> potionList = new GLists<>(0);

    public Hud() {
        super("HUD", Keyboard.KEY_NONE,Category.Render,"Display hud.");
    }

    @Override
    public void onEnable() {
        toggle();
    }

    private boolean updateList, serverUpdate = true, fpsUpdate = true;
    private GLists<Long> hudLists = new GLists<>(3);

    public void drawHUD() {
        if (Config.isGlCalls()) {
            if (mode.getValue() == Mode.FOptimized) {
                mc.fontRendererObj.bindFontTexture();
            } else if (mode.getValue() == Mode.Optimized) {
                GlStateManager.bindTexture(font.textureId());
                GlStateManager.enableBlend();
            }
            if (fpsUpdate) {
                GLCall.compile(hudLists, 0, c -> renderFPS());
                fpsUpdate = false;
            }
            if (debug.getValue()) {
                GLCall.drawLists(globalRenderCalls);
                GLCall.draw(debugLong, DEBUG_MS, c -> renderMS(34));
                GLCall.checkAndCompile(globalLists, DisplayLists.GL_LIST.size(), 0, c -> drawString(StringUtil.combine("GList: ",DisplayLists.GL_LIST.size()), 2, 46, Color.white.getRGB()));
                GLCall.checkAndCompile(globalLists, DisplayLists.GL_LISTS.size(), 1, c -> drawString(StringUtil.combine("GLists: ",DisplayLists.GL_LISTS.size()), 2, 58, Color.white.getRGB()));
                GLCall.drawLists(globalLists);
                /*int y = 86;
                synchronized (EnumWorldBlockLayer.VALUES) {
                    for (EnumWorldBlockLayer layer : EnumWorldBlockLayer.VALUES) {
                        drawString(StringUtil.combine(layer, ": ", mc.renderGlobal.allChunks[layer.ordinal()].size(), ", ", mc.renderGlobal.allChunks[layer.ordinal()].hashCode()), 2, y, Color.white.getRGB());
                        y += 12;
                    }
                }*/
            }
            if (updateList) {
                GLCall.compile(hudLists, 1, c -> renderArrayList());
                updateList = false;
            }
            hudLists.checkAndCompile(Pinger.ping(), 2, c -> renderPing());
            if (potion.getValue()) {
                renderPotion();
            }
            GLCall.drawLists(hudLists);
        } else {
            renderFPS();
            renderArrayList();
        }
    }

    private void renderServer() {
        switch (mode.getValue()) {
            case FOptimized -> mc.fontRendererObj.drawStringWithShadow(mc.getCurrentServerData().serverName, 2, 26, Color.white.getRGB());
            case Optimized -> font.drawStringWithShadow(mc.getCurrentServerData().gameVersion, 2, 26, Color.white);
        }
    }


    private void renderPotion() {
        if (!mc.thePlayer.getActivePotionEffects().isEmpty()) {
            if (potionList.size() != mc.thePlayer.getActivePotionEffects().size() && Config.isGlCalls()) {
                potionList.deleteLists();
                potionList = new GLists<>(mc.thePlayer.getActivePotionEffects().size());
            }
            ScaledResolution resolution = RenderUtils.SCALED_RES;
            int y = resolution.getScaledHeight() - 15;
            int i = 0;
            for (PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
                int finalY = y;
                if (GLCall.checkAndCompile(potionList, effect, i, c -> {
                    Potion potion = Potion.potionTypes[effect.getPotionID()];
                    String duration = Potion.getDurationString(effect);
                    String amplifier = String.valueOf(effect.getAmplifier() + 1);
                    String finalString = I18n.format(potion.getName()) + " §f" + I18n.format("enchantment.level."+amplifier) + "§8 - §f" + duration;
                    drawString(finalString, resolution.getScaledWidth() - getWidth(finalString) - 1, finalY, potion.getLiquidColor());
                })) {
                    potionList.setType(i, new PotionEffect(effect));
                }
                y -= 11;
                i++;
            }
            GLCall.drawLists(potionList);
        }
    }


    private int getWidth(String s) {
        switch (mode.getValue()) {
            case FOptimized -> mc.fontRendererObj.getStringWidth(s);
            case Optimized -> {
                return (int) font.getWidth(s);
            }
        }
        return 0;
    }

    private void drawString(String s, int x, int y, int color) {
        switch (mode.getValue()) {
            case Optimized -> font.drawStringWithShadow(s, x, y, color);
            case FOptimized ->  mc.fontRendererObj.drawStringWithShadow(s, x, y, color);
        }
    }

    private void renderMS(int finalY1) {
        switch (mode.getValue()) {
            case FOptimized -> mc.fontRendererObj.drawString("MS: "+DEBUG_MS, 2, finalY1, Color.white.getRGB());
            case Optimized -> font.drawString("MS: "+DEBUG_MS, 2, finalY1, Color.white.getRGB());
        }
    }
    private void renderLayer(EnumWorldBlockLayer layer, int laylay, int finalY) {
        switch (mode.getValue()) {
            case FOptimized -> mc.fontRendererObj.drawString(layer.name()+" : "+ laylay, 2, finalY, Color.white.getRGB());
            case Optimized -> font.drawString(layer.name()+" : "+ laylay, 2, finalY, Color.white.getRGB());
        }
    }
    private void renderFPS() {
        switch (mode.getValue()) {
            case FOptimized -> mc.fontRendererObj.drawStringWithShadow(StringUtil.combine("FPS : ", Minecraft.getDebugFPS()," (",Minecraft.getLastMs(),"ms)"), 2, 14, Color.white.getRGB());
            case Optimized -> font.drawStringWithShadow(StringUtil.combine("FPS : ", Minecraft.getDebugFPS()," (",Minecraft.getLastMs(),"ms)"), 2, 14, Color.white);
        }
    }
    private void renderPing() {
        switch (mode.getValue()) {
            case FOptimized -> mc.fontRendererObj.drawStringWithShadow(StringUtil.combine("PING : ", Pinger.ping()), 2, 24, Color.white.getRGB());
            case Optimized -> font.drawStringWithShadow(StringUtil.combine("PING : ", Pinger.ping()), 2, 24, Color.white);
        }
    }
    private void renderArrayList() {
        switch (mode.getValue()) {
            case FOptimized -> {
                ScaledResolution sr = RenderUtils.SCALED_RES;
                mc.fontRendererObj.begin();
                mc.fontRendererObj.setAutoBegin(false);
                mc.fontRendererObj.drawStringWithShadow(text.getValue(), 2, 2, Ipana.getClientColor().getRGB());
                final int[] y = {2};
                ModuleManager.getModuleList().stream().filter(mod -> mod.isEnabled() && mod.visible).sorted(Comparator.comparingDouble(mod -> -mod.getWidth())).forEach(module -> {
                    mc.fontRendererObj.drawStringWithShadow(StringUtil.combine(module.getName(),module.getSuffix()), sr.getScaledWidth()-module.getWidth(), y[0], Ipana.getClientColor().getRGB());
                    y[0] += 10;
                });
                mc.fontRendererObj.end();
                mc.fontRendererObj.setAutoBegin(true);
            }
            case Optimized -> {
                ScaledResolution sr = RenderUtils.SCALED_RES;
                font.setAutoBegin(false);
                font.begin();
                font.drawStringWithShadow(text.getValue(), 2, 2, Ipana.getClientColor());
                final int[] y = {2};
                ModuleManager.getModuleList().stream().filter(mod -> mod.isEnabled() && mod.visible).sorted(Comparator.comparingDouble(mod -> -mod.getWidth())).forEach(module -> {
                    font.drawStringWithShadow(StringUtil.combine(module.getName(),module.getSuffix()), sr.getScaledWidth()-module.getWidth(), y[0], Ipana.getClientColor());
                    y[0] += 10;
                });
                font.end();
                font.setAutoBegin(true);
            }
        }
    }

    public void markArraylistDirty() {
        this.updateList = true;
    }

    public void markFPSDirty() {
        this.fpsUpdate = true;
    }

    public void markServerDirty() {
        this.serverUpdate = true;
    }

    public enum Mode {
        FOptimized, Optimized
    }
}
