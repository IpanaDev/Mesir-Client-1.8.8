package ipana.managements.value;

import ipana.managements.module.Module;
import net.minecraft.client.Minecraft;

public abstract class Mode<T extends Module> {
    private String name;
    public Minecraft mc = Minecraft.getMinecraft();
    private T parent;

    public Mode(String name, T parent) {
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public T getParent() {
        return parent;
    }
}
