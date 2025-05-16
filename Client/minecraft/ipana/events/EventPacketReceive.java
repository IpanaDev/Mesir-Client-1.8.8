package ipana.events;

import pisi.unitedmeows.eventapi.event.Event;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

public class EventPacketReceive extends Event{

    private Packet<? extends INetHandler>packet;
    private PacketState state;

    public EventPacketReceive(Packet<? extends INetHandler> packet, PacketState state) {
        this.packet = packet;
        this.state = state;
    }

    public PacketState getState() {
        return this.state;
    }

    public void setState(PacketState state) {
        this.state = state;
    }

    public Packet<? extends INetHandler> getPacket() {
        return this.packet;
    }

    public enum PacketState {
        PRE, POST
    }
}
