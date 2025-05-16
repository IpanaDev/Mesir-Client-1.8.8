package ipana.utils.gl;

import net.minecraft.client.renderer.GLAllocation;
import org.lwjgl.opengl.GL11;

import java.nio.IntBuffer;
import java.util.function.Consumer;

public class GLists<T> {
    private boolean created;
    private int[] displayList;
    private IntBuffer buffer;
    T[] types;

    public GLists(int range) {
        displayList = new int[range];
        for (int i = 0; i < range; i++) {
            displayList[i] = -1;
        }
    }

    public void render() {
        checkList();
        buffer.position(0);
        GL11.glCallLists(buffer);
    }

    public boolean checkAndCompile(T type, int index, Consumer<Boolean> bool) {
        if (check(type, index)) {
            compile(index, bool);
            return true;
        }
        return false;
    }
    public boolean check(T type, int index) {
        checkList();
        if (displayList[index] != -1 && types != null && ((type == null && this.types[index] != null) || (type != null && !type.equals(this.types[index])))) {
            this.types[index] = type;
            return true;
        }
        return false;
    }
    public void compile(int index, Consumer<Boolean> bool) {
        checkList();
        GL11.glNewList(displayList[index], GL11.GL_COMPILE);
        bool.accept(true);
        GL11.glEndList();
    }
    public void genList(int range) {
        buffer = GLAllocation.createDirectIntBuffer(range);
        types = (T[]) new Object[range];
        for (int i = 0; i < range; i++) {
            if (displayList[i] == -1) {
                displayList[i] = GLAllocation.generateDisplayLists(1);
                buffer.put(displayList[i]);
            }
        }
        DisplayLists.GL_LISTS.add(this);
    }

    public void checkList() {
        if (!created) {
            genList(displayList.length);
            created = true;
        }
    }

    public int size() {
        return displayList.length;
    }

    public void setType(int index, T type) {
        types[index] = type;
    }

    public void deleteLists() {
        deleteLists(true);
    }

    public void deleteLists(boolean deleteFromList) {
        if (created) {
            if (deleteFromList) {
                DisplayLists.GL_LISTS.remove(this);
            }
            buffer.clear();
            //types = null;
            created = false;
            for (int i = 0; i < displayList.length; i++) {
                if (displayList[i] != -1) {
                    GLAllocation.deleteDisplayLists(displayList[i]);
                    setType(i, null);
                    displayList[i] = -1;
                }
            }
        }
    }
}
