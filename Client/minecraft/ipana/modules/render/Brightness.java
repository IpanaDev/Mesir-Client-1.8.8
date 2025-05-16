package ipana.modules.render;

import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.BoolValue;
import org.lwjgl.input.Keyboard;

public class Brightness extends Module {
    public Brightness() {
        super("Brightness", Keyboard.KEY_NONE,Category.Render,"Everything is bright");
    }

    public BoolValue funny = new BoolValue("Funny",this,false,"Changes the vbo color pointer.");

    @Override
    public void onEnable() {
        mc.gameSettings.gammaSetting = 100;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.gameSettings.gammaSetting = 1;
        super.onDisable();
    }
}
