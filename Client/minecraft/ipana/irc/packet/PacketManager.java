package ipana.irc.packet;

import java.util.ArrayList;
import java.util.List;

import static ipana.irc.packet.Packets.*;

public class PacketManager {
    private static final List<Packet> packets = new ArrayList<>();


    public static void addPackets() {
        packets.add(RECEIVE_PROPERTY);
        packets.add(SEND_PROPERTY);
        packets.add(EMOTE_PACKET);
        packets.add(JOIN_PACKET);
        packets.add(COSMETIC_PACKET);
        packets.add(LEAVE_PACKET);
    }

    public static List<Packet> packets() {
        return packets;
    }
}
