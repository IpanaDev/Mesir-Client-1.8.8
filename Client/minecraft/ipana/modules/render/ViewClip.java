package ipana.modules.render;

import ipana.managements.module.Category;
import ipana.managements.module.Module;
import org.lwjgl.input.Keyboard;

public class ViewClip extends Module {
    public ViewClip() {
        super("ViewClip", Keyboard.KEY_NONE, Category.Render, "Disable clip bounds.");
    }
}
