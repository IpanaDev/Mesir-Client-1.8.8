package ipana.irc.packet.packets;

import ipana.irc.user.User;
import ipana.irc.user.UserProperties;
import ipana.irc.packet.Packet;

public class ReceivePropertyPacket extends Packet {

    @Override
    public String name() {
        return "sendUserProperty";
    }

    @Override
    public void execute(User sender, String... args) {
        for (String arg : args) {
            String[] split = arg.split("=");
            sender.setProperty(UserProperties.valueOf(split[0]), split[1]);
        }
    }

    @Override
    public boolean hasArgs() {
        return true;
    }
}
