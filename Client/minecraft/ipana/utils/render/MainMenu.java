package ipana.utils.render;

import ipana.utils.gif.GIF;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class MainMenu {
    public static final MainMenu INSTANCE = new MainMenu();
    public int gifX;
    public int gifY;
    public boolean mX;
    public boolean mY;
    public GIF menu7 = new GIF("ezgif-frame",158);

    public MainMenu() {
        long preMs = System.currentTimeMillis();
        for (ResourceLocation location : menu7.locations) {
            System.out.println(location.getResourcePath());

            Minecraft.getMinecraft().getTextureManager().bindTexture(location);
        }
        double ms = (System.currentTimeMillis()-preMs)/1000d;
        System.out.println("Took "+ms+" seconds to load textures");
    }
}
