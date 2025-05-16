package ipana.utils.gl;

import net.minecraft.client.renderer.GLAllocation;
import org.lwjgl.opengl.GL11;

import java.util.function.Consumer;

public class GList<T> {
    private int displayList = -1;
    T type;

    public void render(T type, Consumer<Boolean> bool) {
        int list = genList();
        checkAndCompile(type, bool);
        GL11.glCallList(list);
    }
    public void render() {
        int list = genList();
        GL11.glCallList(list);
    }
    public void checkAndCompile(T type, Consumer<Boolean> bool) {
        if ((type == null && this.type != null) || (type != null && !type.equals(this.type))) {
            compile(bool);
            this.type = type;
        }
    }
    public void compile(Consumer<Boolean> bool) {
        GL11.glNewList(genList(), GL11.GL_COMPILE);
        bool.accept(true);
        GL11.glEndList();
    }
    public int genList() {
        if (displayList == -1) {
            displayList = GLAllocation.generateDisplayLists(1);
            DisplayLists.GL_LIST.add(this);
        }
        return displayList;
    }
    public void deleteList() {
        deleteList(true);
    }
    public void deleteList(boolean deleteFromList) {
        if (displayList != -1) {
            if (deleteFromList) {
                DisplayLists.GL_LIST.remove(this);
            }
            GLAllocation.deleteDisplayLists(displayList);
            setType(null);
            displayList = -1;
        }
    }
    public T type() {
        return type;
    }

    public void setType(T _type) {
        type = _type;
    }
}
