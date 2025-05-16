package ipana.irc.packet;

import ipana.irc.packet.packets.*;

public class Packets {

    public static final ReceivePropertyPacket RECEIVE_PROPERTY = new ReceivePropertyPacket();
    public static final SendPropertyPacket SEND_PROPERTY = new SendPropertyPacket();
    public static final EmotePacket EMOTE_PACKET = new EmotePacket();
    public static final JoinPacket JOIN_PACKET = new JoinPacket();
    public static final CosmeticPacket COSMETIC_PACKET = new CosmeticPacket();
    public static final LeavePacket LEAVE_PACKET = new LeavePacket();
}
