package ipana.bot;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.function.Consumer;

import static net.minecraft.network.EnumConnectionState.LOGIN;

public class BotServer {
    private String ip;
    private int port;
    private InetAddress inetAddress;

    public BotServer(String ip, int port) {
        try {
            this.ip = ip;
            this.port = port;
            this.inetAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public void newQuery(String name, Consumer<NetworkManager> onSuccess, Runnable onFail) {
        newQuery(BotWrapper.fromName(name), onSuccess, onFail);
    }

    public void newQuery(GameProfile profile, Consumer<NetworkManager> onSuccess, Runnable onFail) {
        var networkManager = NetworkManager.func_181124_a(inetAddress, port, true);
        networkManager.setNetHandler(new LoginListener(networkManager, onSuccess, onFail));
        networkManager.sendPacket(new C00Handshake(47, ip, port, LOGIN));
        networkManager.sendPacket(new C00PacketLoginStart(profile));
        while (networkManager.isChannelOpen()) {
            //Wait
        }
    }
}
