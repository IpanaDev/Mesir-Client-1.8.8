package ipana.utils.net;

import ipana.utils.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.status.INetHandlerStatusClient;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.network.status.server.S00PacketServerInfo;
import net.minecraft.network.status.server.S01PacketPong;
import net.minecraft.util.IChatComponent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Pinger {
    private static Thread thread;
    private static final Runnable run;
    private static boolean isEnabled;
    private static long ping;
    private static String cachedIP;
    private static int cachedPort;
    private static InetSocketAddress cachedAdress;

    static {
        run = () -> {
            while (true) {
                if (isEnabled && !Minecraft.getMinecraft().isSingleplayer() && Minecraft.getMinecraft().getCurrentServerData() != null) {
                    ServerAddress serveraddress = ServerAddress.func_78860_a(Minecraft.getMinecraft().getCurrentServerData().serverIP);
                    String ip = serveraddress.getIP();
                    int port = serveraddress.getPort();
                    if (cachedPort != port || !cachedIP.equals(ip)) {
                        cachedAdress = new InetSocketAddress(ip, port);
                        cachedPort = port;
                        cachedIP = ip;
                    }
                    /*try {
                        NetworkManager networkManager = NetworkManager.func_181124_a(InetAddress.getByName(ip), port, true);
                        networkManager.setNetHandler(new INetHandlerStatusClient() {
                            @Override
                            public void onDisconnect(IChatComponent reason) {
                                //System.out.println(reason);
                            }

                            @Override
                            public void handleServerInfo(S00PacketServerInfo packetIn) {
                            }

                            @Override
                            public void handlePong(S01PacketPong packetIn) {
                                ping = Minecraft.getSystemTime() - packetIn.clientTime();
                            }
                        });
                        networkManager.sendPacket(new C00Handshake(47, serveraddress.getIP(), serveraddress.getPort(), EnumConnectionState.STATUS));
                        networkManager.sendPacket(new C00PacketServerQuery());
                        networkManager.sendPacket(new C01PacketPing(Minecraft.getSystemTime()));
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }*/

                    try (final Socket socket = new Socket()) {
                        socket.setKeepAlive(false);
                        socket.setTcpNoDelay(true);
                        long start = System.currentTimeMillis();
                        socket.connect(cachedAdress, 1000);
                        ping = System.currentTimeMillis() - start;
                        //System.out.println("ping to server ("+ip+":"+port+") -> " + ping);
                    } catch (IOException e) {
                        //throw new RuntimeException(e);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) { }
            }
        };

        thread = new Thread(run);
        thread.start();
    }

    public static long ping() {
        return ping;
    }

    public static void setEnabled(boolean state) {
        isEnabled = state;
    }

    @Deprecated
    public static void start() {
        if (thread == null) {
            thread = new Thread(run);
            thread.start();
        } else {
            PlayerUtils.debug("Pinger thread is not null and start method called");
        }
    }

    @Deprecated
    public static void stop() {
        if (thread != null) {
            thread.interrupt();
            thread = null;
        } else {
            PlayerUtils.debug("Pinger thread is null and stop method called");
        }
    }
}
