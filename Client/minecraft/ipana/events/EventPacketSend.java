package ipana.events;


import pisi.unitedmeows.eventapi.event.Event;
import net.minecraft.network.Packet;

public class EventPacketSend extends Event{
    private Packet packet;
    private PacketState state;

    public EventPacketSend(Packet packet,PacketState state) {
        this.packet = packet;
        this.state = state;
    }

    public PacketState getState() {
        return this.state;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket() {
        return this.packet;
    }

    public enum PacketState {
        PRE,POST
    }

}
