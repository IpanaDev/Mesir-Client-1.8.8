package ipana.renders.account;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.profiler.Profiler;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldSettings;

public class WorldButFake extends WorldClient {
    public WorldButFake(FakeGoBrr p_i45063_1_, WorldSettings p_i45063_2_) {
        super(p_i45063_1_, p_i45063_2_, 0, EnumDifficulty.HARD, new Profiler());
        this.provider.registerWorld(this);
    }
}
