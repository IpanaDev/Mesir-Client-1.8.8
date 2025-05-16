package ipana.utils.gl;

import ipana.utils.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class DisplayLists {
    public static final ArrayList<GList<?>> GL_LIST = new ArrayList<>();
    public static final ArrayList<GLists<?>> GL_LISTS = new ArrayList<>();
    public static final boolean CLEAR_ALL = true;
    public static final int GC_TICKS = 100;
    private static int lastClearedTick;

    public static void gc() {
        /*
        if (CLEAR_ALL) {
            if (Minecraft.getRunTick()-lastClearedTick >= GC_TICKS) {
                clearAll();
                lastClearedTick = Minecraft.getRunTick();
            }
        } else {
            for (int i = 0; i < GL_LIST.size(); i++) {
                GList<?> gList = GL_LIST.get(i);
                if (gList.dirty()) {
                    gList.deleteList();
                    i--;
                }
            }
            for (int i = 0; i < GL_LISTS.size(); i++) {
                GLists<?> gList = GL_LISTS.get(i);
                if (gList.dirty()) {
                    gList.deleteLists();
                    i--;
                }
            }
        }

         */
    }

    public static void clearAll() {
        for (GList<?> gList : GL_LIST) {
            gList.deleteList(false);
        }
        for (GLists<?> gList : GL_LISTS) {
            gList.deleteLists(false);
        }
        GL_LIST.clear();
        GL_LISTS.clear();
    }
}
