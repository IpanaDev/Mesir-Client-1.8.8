package ipana.utils.ncp.utilities;

import net.minecraft.client.Minecraft;

public class AccountEntry {
    public final int tick;
    public double value;
    public double sum = 0.0;
    public int actCount;
    public int valCount;

    public AccountEntry(double value, int actCount, int valCount) {
        this.tick = Minecraft.getRunTick();
        this.value = value;
        this.actCount = actCount;
        this.valCount = valCount;
    }

    public AccountEntry(int tick, double value, int actCount, int valCount) {
        this.tick = tick;
        this.value = value;
        this.actCount = actCount;
        this.valCount = valCount;
    }

    public String toString() {
        return "AccountEntry(tick=" + this.tick + " sum=" + this.sum + " value=" + this.value + " valid=" + this.valCount + " activate=" + this.actCount + ")";
    }
}

