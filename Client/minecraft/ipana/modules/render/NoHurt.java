package ipana.modules.render;

import ipana.managements.module.Category;
import ipana.managements.module.Module;
import org.lwjgl.input.Keyboard;

public class NoHurt extends Module {
    public NoHurt() {
        super("NoHurt", Keyboard.KEY_NONE, Category.Render,"No hurt animation.");
    }

}
