package ipana.renders.ide.settings;

import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class SharedAttributes {
    public static int WINDOW_WIDTH = -1;
    public static int WINDOW_HEIGHT = -1;
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static void updateWindowSizes() {
        ScaledResolution sr = RenderUtils.SCALED_RES;
        WINDOW_WIDTH = sr.getScaledWidth();
        WINDOW_HEIGHT = sr.getScaledHeight();
    }
}
