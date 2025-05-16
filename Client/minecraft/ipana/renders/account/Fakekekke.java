package ipana.renders.account;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;

import java.util.UUID;

public class Fakekekke extends AbstractClientPlayer {

    private NetworkPlayerInfo playerInfo;
    private FakeGoBrr fakeNetty;

    public Fakekekke(FakeGoBrr fakeNetty, World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
        this.fakeNetty = fakeNetty;
    }

    protected NetworkPlayerInfo getPlayerInfo()
    {
        if (this.playerInfo == null)
        {
            this.playerInfo = fakeNetty.getPlayerInfo(this.getUniqueID());
        }

        return this.playerInfo;
    }

    public boolean isSpectator()
    {
        NetworkPlayerInfo networkplayerinfo = fakeNetty.getPlayerInfo(this.getGameProfile().getId());
        return networkplayerinfo != null && networkplayerinfo.getGameType() == WorldSettings.GameType.SPECTATOR;
    }
}
