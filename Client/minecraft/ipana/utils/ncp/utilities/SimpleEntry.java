package ipana.utils.ncp.utilities;

import net.minecraft.client.Minecraft;

public class SimpleEntry {
    public final int tick;
    public final double value;
    public int actCount;

    public SimpleEntry(double value, int actCount) {
        this.tick = Minecraft.getRunTick();
        this.value = value;
        this.actCount = actCount;
    }

    public SimpleEntry(int tick, double value, int actCount) {
        this.tick = tick;
        this.value = value;
        this.actCount = actCount;
    }

    public String toString() {
        return "SimpleEntry(tick=" + this.tick + " value=" + this.value + " activate=" + this.actCount + ")";
    }
}

