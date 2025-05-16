package net.minecraft.client.renderer;

import com.google.common.primitives.Floats;
import java.util.Comparator;

class WorldRenderer$1 implements Comparator<Integer>
{
    final float[] floats;
    final WorldRenderer worldRenderer;

    WorldRenderer$1(WorldRenderer p_i46500_1_, float[] p_i46500_2_)
    {
        this.worldRenderer = p_i46500_1_;
        this.floats = p_i46500_2_;
    }

    public int compare(Integer p_compare_1_, Integer p_compare_2_) {
        return Floats.compare(this.floats[p_compare_2_], this.floats[p_compare_1_]);
    }
}
