package ipana.irc.packet.packets;

import ipana.Ipana;
import ipana.irc.user.User;
import ipana.irc.packet.Packet;

public class SendPropertyPacket extends Packet {
    @Override
    public String name() {
        return "requestUserProperty";
    }

    @Override
    public void execute(User sender, String... args) {
        Ipana.mainIRC().self().sendProperties();
        Ipana.mainIRC().self().cosmetics().sendPacket();
    }

    @Override
    public boolean hasArgs() {
        return false;
    }
}
