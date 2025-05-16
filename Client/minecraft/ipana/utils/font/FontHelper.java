package ipana.utils.font;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.*;

public class FontHelper {

    public static FontUtil SIZE_18, SIZE_12, SIZE_48, SIZE_18_BOLD, SIZE_15, SIZE_24_BOLD, SIZE_20, eras;
    private static ResourceLocation location;

    public static void setupFont() {
        Minecraft.setStatus("Font");
        try {
            location = new ResourceLocation("mesir/font.ttf");
            SIZE_12 = new FontUtil(createFont(12), true, FontUtil.Style.REGULAR);
            SIZE_15 = new FontUtil(createFont(15), true, FontUtil.Style.REGULAR);
            SIZE_18 = new FontUtil(createFont(18), true, FontUtil.Style.REGULAR);
            SIZE_18_BOLD = new FontUtil(createFont(18), true, FontUtil.Style.BOLD);
            SIZE_20 = new FontUtil(createFont(20), true, FontUtil.Style.REGULAR);
            SIZE_24_BOLD = new FontUtil(createFont(24), true, FontUtil.Style.BOLD);
            SIZE_48 = new FontUtil(createFont(48), true, FontUtil.Style.BOLD);
            location = new ResourceLocation("mesir/eras.ttf");
            eras = new FontUtil(createFont(96), true, FontUtil.Style.ITALIC);
            System.out.println("Fonts loaded.");
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
        }
    }


    public static Font createFont(int size) throws IOException, FontFormatException {
        IResource iResource = Minecraft.getMinecraft().getTextureManager().theResourceManager.getResource(location);
        Font f = Font.createFont(Font.TRUETYPE_FONT, iResource.getInputStream()).deriveFont(Font.PLAIN, size);
        iResource.getInputStream().close();
        return f;
    }

}
