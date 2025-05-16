package ipana.utils.gl;

import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.function.Consumer;

public class VList<T> {
    ArrayList<Integer> clientStates = new ArrayList<>();
    Consumer<Boolean> arrayPointers;
    VertexBuffer vertexBuffer;
    int mode;
    T type;

    public VList(int mode, VertexFormat vertexFormat, Consumer<Boolean> arrayPointers) {
        this.mode = mode;
        this.vertexBuffer = new VertexBuffer(vertexFormat);
        this.arrayPointers = arrayPointers;
        for (VertexFormatElement element : vertexFormat.getElements()) {
            if (element == DefaultVertexFormats.POSITION_3F) {
                clientStates.add(GL11.GL_VERTEX_ARRAY);
            } else if (element == DefaultVertexFormats.TEX_2F) {
                clientStates.add(GL11.GL_TEXTURE_COORD_ARRAY);
            } else if (element == DefaultVertexFormats.COLOR_4UB) {
                clientStates.add(GL11.GL_COLOR_ARRAY);
            } else if (element == DefaultVertexFormats.NORMAL_3B) {
                clientStates.add(GL11.GL_NORMAL_ARRAY);
            }
        }
        System.out.println("New VList created. State size: "+clientStates.size());
    }
    public void render() {
        for (int i : clientStates) {
            GL11.glEnableClientState(i);
        }
        vertexBuffer.bindBuffer();
        arrayPointers.accept(true);
        vertexBuffer.drawArrays(this.mode);
        vertexBuffer.unbindBuffer();
        for (int i : clientStates) {
            GL11.glDisableClientState(i);
        }
    }
    public void render(T type, ByteBuffer bool) {
        checkAndCompile(type, bool);
        this.render();
    }

    public void checkAndCompile(T type, ByteBuffer bool) {
        if ((type == null && this.type != null) || (type != null && !type.equals(this.type))) {
            compile(bool);
            this.type = type;
        }
    }
    public void compile(ByteBuffer buffer) {
        vertexBuffer.uploadBufferData(buffer);
    }

    public void deleteList() {
        vertexBuffer.deleteGlBuffers();
    }
}
