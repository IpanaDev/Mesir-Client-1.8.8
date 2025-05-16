package ipana.renders.account;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.NetworkManager;

import java.util.UUID;

public class FakeGoBrr extends NetHandlerPlayClient {
    private NetworkPlayerInfo playerInfo;

    public FakeGoBrr(Minecraft mc, GameProfile profile) {
        super(mc, mc.currentScreen, new NetworkManager(), profile);
        this.playerInfo = new NetworkPlayerInfo(profile);
    }

    public NetworkPlayerInfo getPlayerInfo(UUID uuid) {
        return playerInfo;
    }
    public NetworkPlayerInfo getPlayerInfo(String name) {
        return playerInfo;
    }
}
