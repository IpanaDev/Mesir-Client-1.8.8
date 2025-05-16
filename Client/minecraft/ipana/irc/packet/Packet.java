package ipana.irc.packet;

import ipana.Ipana;
import ipana.irc.user.User;

import java.util.Arrays;

public abstract class Packet {

    public abstract String name();

    public abstract void execute(User sender, String... args);

    public abstract boolean hasArgs();

    public void sendAsPacket(String... args) {
        Ipana.mainIRC().msg("$"+name()+" "+Arrays.toString(args).replace(" ",""));
    }
}
