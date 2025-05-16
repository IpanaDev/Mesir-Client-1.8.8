package ipana.utils.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.WorldRenderer;
import optifine.Config;

public class BufferBridge {
    WorldRenderer worldRenderer;

    public BufferBridge(WorldRenderer worldRenderer) {
        this.worldRenderer = worldRenderer;
    }
    public void putFloat(int index, float value) {
        if (Config.isDirectMemory()) {
            Minecraft.getMinecraft().UNSAFE.putFloat(worldRenderer.bufferPointer+index, value);
        } else {
            worldRenderer.getByteBuffer().putFloat(index, value);
        }
    }
    public void putDouble(int index, double value) {
        if (Config.isDirectMemory()) {
            Minecraft.getMinecraft().UNSAFE.putDouble(worldRenderer.bufferPointer+index, value);
        } else {
            worldRenderer.getByteBuffer().putDouble(index, value);
        }
    }
    public void putInt(int index, int value) {
        if (Config.isDirectMemory()) {
            Minecraft.getMinecraft().UNSAFE.putInt(worldRenderer.bufferPointer+index, value);
        } else {
            worldRenderer.getByteBuffer().putInt(index, value);
        }
    }
    public void putShort(int index, short value) {
        if (Config.isDirectMemory()) {
            Minecraft.getMinecraft().UNSAFE.putShort(worldRenderer.bufferPointer+index, value);
        } else {
            worldRenderer.getByteBuffer().putShort(index, value);
        }
    }
    public void put(int index, byte value) {
        if (Config.isDirectMemory()) {
            Minecraft.getMinecraft().UNSAFE.putByte(worldRenderer.bufferPointer+index, value);
        } else {
            worldRenderer.getByteBuffer().put(index, value);
        }
    }
}
