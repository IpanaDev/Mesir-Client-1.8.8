package ipana.utils.gif;

import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GIF {
    public List<ResourceLocation> locations = new ArrayList<>();
    private long ms;
    public int ticks;

    public GIF(String name, int frame) {
        for (int i = 1; i <= frame; i++) {
            locations.add(new ResourceLocation("mesir/mainmenu/mainmenu7frames/"+name+" ("+i+").jpg"));
        }
    }
    public GIF(String path, String name, int frame, String fileType) {
        for (int i = 1; i <= frame; i++) {
            locations.add(new ResourceLocation("mesir/"+path+"/"+name+" ("+i+")."+fileType));
        }
    }
    public ResourceLocation getFrame(int frameTime) {
        if (System.currentTimeMillis()-ms >= frameTime) {
            ticks++;
            if (ticks >= locations.size()) {
                ticks = 0;
            }
            ms = System.currentTimeMillis();
        }
        return locations.get(ticks);
    }
}
