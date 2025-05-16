package ipana.bot;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.INetHandlerLoginClient;
import net.minecraft.network.login.server.S00PacketDisconnect;
import net.minecraft.network.login.server.S01PacketEncryptionRequest;
import net.minecraft.network.login.server.S02PacketLoginSuccess;
import net.minecraft.network.login.server.S03PacketEnableCompression;
import net.minecraft.util.IChatComponent;

import java.util.Queue;
import java.util.function.Consumer;

public class LoginListener implements INetHandlerLoginClient {
    private NetworkManager networkManager;
    private Consumer<NetworkManager> onSuccess;
    private Runnable onFail;

    public LoginListener(NetworkManager networkManager, Consumer<NetworkManager> onSuccess, Runnable onFail) {
        this.networkManager = networkManager;
        this.onSuccess = onSuccess;
        this.onFail = onFail;
    }

    @Override
    public void handleEncryptionRequest(S01PacketEncryptionRequest packetIn) {

    }

    @Override
    public void handleLoginSuccess(S02PacketLoginSuccess packetIn) {
        var gameProfile = packetIn.getProfile();
        onSuccess.accept(networkManager);
    }

    @Override
    public void handleDisconnect(S00PacketDisconnect packetIn) {
        onFail.run();
        networkManager.closeChannel(packetIn.func_149603_c());
        System.out.println("Disconnect on login: "+packetIn.func_149603_c().getUnformattedText());
        //response.add(new LoginResponse(false, null, packetIn.func_149603_c()));
    }

    @Override
    public void handleEnableCompression(S03PacketEnableCompression packetIn) {
        //System.out.println("S03PacketEnableCompression");
        if (!networkManager.isLocalChannel()) {
            networkManager.setCompressionTreshold(packetIn.getCompressionTreshold());
        }
    }

    @Override
    public void onDisconnect(IChatComponent reason) {

    }
}
