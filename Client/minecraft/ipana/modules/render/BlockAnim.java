package ipana.modules.render;

import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.EnumValue;
import org.lwjgl.input.Keyboard;

public class BlockAnim extends Module {
    public BlockAnim() {
        super("BlockAnim", Keyboard.KEY_NONE, Category.Render,"Block hit animation.");
        setEnabled(true);
    }
    public EnumValue<Mode> mode = new EnumValue<>("Mode",this,Mode.class,"Animation mode.");

    @Override
    public void onToggled() {
        setEnabled(false);
    }

    @Override
    public void onSuffixChange() {
        setSuffix(mode.getValue().enumName());
        super.onSuffixChange();
    }

    public enum Mode{
        OLD("1.7"),
        NEW("1.8"),
        CUSTOM("Custom");
        private String enumName;

        Mode(String enumName) {
            this.enumName = enumName;
        }

        public String enumName() {
            return enumName;
        }
    }
}
