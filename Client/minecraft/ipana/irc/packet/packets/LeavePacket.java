package ipana.irc.packet.packets;

import ipana.Ipana;
import ipana.irc.packet.Packet;
import ipana.irc.user.User;
import ipana.renders.ingame.Line;

public class LeavePacket extends Packet {
    @Override
    public String name() {
        return "leave";
    }

    @Override
    public void execute(User sender, String... args) {
        sender.irc().ircChat().printChatMessage(Line.create(sender.senderName(), "left.", sender));
        Ipana.mainIRC().users().remove(sender);
    }

    @Override
    public boolean hasArgs() {
        return false;
    }
}
