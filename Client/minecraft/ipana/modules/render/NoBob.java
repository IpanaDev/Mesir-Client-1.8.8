package ipana.modules.render;

import ipana.events.EventRender3D;
import ipana.events.EventTick;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.Value;
import ipana.managements.value.values.EnumValue;
import ipana.managements.value.values.ModeValue;
import org.lwjgl.input.Keyboard;

public class NoBob extends Module {
    public NoBob() {
        super("NoBob", Keyboard.KEY_NONE,Category.Render,"No Bobbing.");
    }

    public EnumValue<Mode> mode = new EnumValue<>("Mode",this,Mode.class,"No bobbing modes");

    @Override
    public void onSuffixChange() {
        setSuffix(mode.getValue().name());
        super.onSuffixChange();
    }

    public enum Mode {
        Hand, Camera
    }
}
