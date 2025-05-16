package ipana.clickgui.autistic;

import ipana.Ipana;
import ipana.clickgui.autistic.panels.CategoryPanel;
import ipana.managements.module.Modules;
import ipana.utils.font.FontHelper;
import ipana.utils.font.FontUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;

public class GuiSettings {

    private static FontUtil categoryFont;
    private static FontUtil normalFont;
    public static Color CLIENT_COLOR;
    public static Color CLIENT_COLOR_OPACITY;
    public static Color CATEGORY_COLOR;
    public static Color CATEGORY_COLOR2;
    public static Color MODULE_ENABLED;
    public static Color MODULE_DISABLED;
    public static Color COL_VALUE1;
    public static Color COL_VALUE2;
    public static Color COL_VALUE3;
    public static Color GRADIENT_START;
    public static Color GRADIENT_END;

    public static void updateColors(CategoryPanel category) {
        float opacity = category.prevTransparent + (category.transparent - category.prevTransparent) * Minecraft.getMinecraft().timer.renderPartialTicks;
        float max = 255;
        float red = Math.min(1, Ipana.getClientColor().getRed()/max);
        float green = Math.min(1, Ipana.getClientColor().getGreen()/max);
        float blue = Math.min(1, Ipana.getClientColor().getBlue()/max);

        CLIENT_COLOR = setColor(CLIENT_COLOR, red, green, blue, opacity);
        CLIENT_COLOR_OPACITY = setColor(CLIENT_COLOR_OPACITY, red, green, blue, 0);
        GRADIENT_START = setColor(GRADIENT_START, red, green, blue, opacity);
        GRADIENT_END = setColor(GRADIENT_END, 20/max, 20/max, 20/max, 0);
        if (Modules.CLICK_GUI.darkTheme.getValue()) {
            CATEGORY_COLOR = setColor(CATEGORY_COLOR, 75/max, 75/max, 75/max, opacity);
            CATEGORY_COLOR2 = setColor(CATEGORY_COLOR2, 110/max, 110/max, 110/max, opacity);
            MODULE_ENABLED = setColor(MODULE_ENABLED, 75/max, 75/max, 75/max, opacity);
            MODULE_DISABLED = setColor(MODULE_DISABLED, 190/max, 190/max, 190/max, opacity);
            COL_VALUE1 = setColor(COL_VALUE1, 64/max, 64/max, 64/max, opacity);
            COL_VALUE2 = setColor(COL_VALUE2, 128/max, 128/max, 128/max, opacity);
            COL_VALUE3 = setColor(COL_VALUE3, 192/max, 192/max, 192/max, opacity);
        } else {
            CATEGORY_COLOR = setColor(CATEGORY_COLOR, 220/max, 220/max, 220/max, opacity);
            CATEGORY_COLOR2 = setColor(CATEGORY_COLOR2, 200/max, 200/max, 200/max, opacity);
            MODULE_ENABLED = setColor(MODULE_ENABLED, 255/max, 255/max, 255/max, opacity);
            MODULE_DISABLED = setColor(MODULE_DISABLED, 80/max, 80/max, 80/max, opacity);
            COL_VALUE1 = setColor(COL_VALUE1, 216/max, 216/max, 216/max, opacity);
            COL_VALUE2 = setColor(COL_VALUE2, 192/max, 192/max, 192/max, opacity);
            COL_VALUE3 = setColor(COL_VALUE3, 128/max, 128/max, 128/max, opacity);
        }
    }

    public static void initFont() {
        normalFont = FontHelper.SIZE_15;
        categoryFont = FontHelper.SIZE_20;
    }

    private static Color setColor(Color color, float red, float green, float blue, float opacity) {
        if (color == null || color.getRed() != (int)(red*255) || color.getGreen() != (int)(green*255) || color.getBlue() != (int)(blue*255) || color.getAlpha() != (int)(opacity*255)) {
            return new Color(red, green, blue, opacity);
        }
        return color;
    }

    public static FontUtil getCategoryFont() {
        return categoryFont;
    }

    public static FontUtil getNormalFont() {
        return normalFont;
    }
}
