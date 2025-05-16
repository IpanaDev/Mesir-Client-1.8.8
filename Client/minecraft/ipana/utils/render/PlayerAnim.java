package ipana.utils.render;

import net.minecraft.entity.player.EntityPlayer;

public class PlayerAnim {
    private String name;
    private long ms;
    private EntityPlayer player;

    public PlayerAnim(String name, long ms, EntityPlayer player) {
        this.name = name;
        this.ms = ms;
        this.player = player;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public long getMs() {
        return ms;
    }

    public String getName() {
        return name;
    }
}
