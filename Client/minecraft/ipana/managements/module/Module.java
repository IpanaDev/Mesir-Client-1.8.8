package ipana.managements.module;


import ipana.eventapi.EventManager;
import ipana.modules.render.Hud;
import ipana.utils.font.FontHelper;
import ipana.utils.math.MathUtils;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Util;
import java.awt.*;
import java.util.function.Consumer;

public class Module {

    private int key;
    private boolean enabled;
    private Category category;
    private String name;
    private String description;
    private String suffix;
    protected Minecraft mc = Minecraft.getMinecraft();
    public int arrayListX;
    private Color color;
    private float width;
    public boolean visible;

    public Module(String name,int key,Category category,String description) {
        this.name = name;
        this.key = key;
        this.category = category;
        this.description = description;
        arrayListX = displayWidth();
        suffix = "";
        color = Color.white;
        width = FontHelper.SIZE_18.getWidth(name);
        visible = true;
    }
    public Module(String name, int key, Category category, String description, Consumer<Module> init) {
        this.name = name;
        this.key = key;
        this.category = category;
        this.description = description;
        arrayListX = displayWidth();
        suffix = "";
        color = Color.white;
        width = FontHelper.SIZE_18.getWidth(name);
        visible = true;
        init.accept(this);
    }
    public Module() {

    }

    public void toggle() {
        Modules.HUD.markArraylistDirty();
        onToggled();
        enabled=!enabled;
        if (enabled) {
            onSuffixChange();
            onEnable();
        } else {
            onDisable();
        }
    }

    public void onToggled() {

    }

    public void onDisable() {
        EventManager.eventSystem.unsubscribeAll(this);
    }

    public void onEnable() {
        int r = MathUtils.random(0,255);
        int g = MathUtils.random(0,255);
        int b = MathUtils.random(0,255);
        color = new Color(r,g,b);
        EventManager.eventSystem.subscribeAll(this);
        ScaledResolution sr = RenderUtils.SCALED_RES;
        if (mc.thePlayer != null) {
            if (arrayListX >= sr.getScaledWidth()) {
                arrayListX = sr.getScaledWidth() - 1;
            }
        } else {
            if (arrayListX >= displayWidth() / 2) {
                arrayListX = displayWidth() / 2 - 1;
            }
        }
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        Hud hud = Modules.HUD;

        switch (hud.mode.getValue()) {
            case Optimized -> this.suffix = " §f<" + suffix + ">";
            case FOptimized -> this.suffix = " §7(§f" + suffix + "§7)";
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void onSuffixChange() {
        Hud hud = Modules.HUD;
        switch (hud.mode.getValue()) {
            case Optimized -> width = FontHelper.SIZE_18.getWidth(this.name + this.suffix);
            case FOptimized -> width = mc.fontRendererObj.getStringWidth(this.name + this.suffix);
        }
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    private int displayWidth() {
        return Util.getOSType() == Util.EnumOS.WINDOWS ? (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() : RenderUtils.SCALED_RES.getScaledWidth();
    }
}
