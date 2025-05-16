package net.minecraft.network;

import ipana.events.EventPacketReceive;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.IThreadListener;

public class PacketThreadUtil
{
    private static Minecraft mc = Minecraft.getMinecraft();

    public static <T extends INetHandler> void checkThreadAndEnqueue(final Packet<T> packet, final T netHandler, IThreadListener threadListener) throws ThreadQuickExitException {
        if (!threadListener.isCallingFromMinecraftThread()) {
            threadListener.addScheduledTask(() -> {
                if (netHandler instanceof INetHandlerPlayClient) {
                    EventPacketReceive pre = new EventPacketReceive(packet, EventPacketReceive.PacketState.PRE);
                    pre.fire();
                    if (pre.isCancelled()) {
                        return;
                    }
                }
                packet.processPacket(netHandler);
                if (netHandler instanceof INetHandlerPlayClient) {
                    EventPacketReceive post = new EventPacketReceive(packet, EventPacketReceive.PacketState.POST);
                    post.fire();
                }
            });
            throw ThreadQuickExitException.field_179886_a;
        }
    }
}
