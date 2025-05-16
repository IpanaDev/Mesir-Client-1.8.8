package ipana.irc.user;

import ipana.irc.IRC;
import ipana.irc.packet.Packets;

import java.util.HashMap;

public class User {
    private HashMap<UserProperties, String> properties = new HashMap<>();
    private IRC irc;
    private String senderName;
    private Parser parser = new Parser(this);
    private PlayerCosmetics cosmetics;

    public User(IRC irc) {
        this.irc = irc;
        cosmetics = new PlayerCosmetics(this, 0);
    }

    public void requestOnJoin() {
        //sifreCal();
        sendProperties();
        requestProperties();
        Packets.JOIN_PACKET.sendAsPacket();
    }

    public void sendProperties() {
        Packets.RECEIVE_PROPERTY.sendAsPacket(parser.parseProperties());
    }
    public void sendProperty(UserProperties property) {
        Packets.RECEIVE_PROPERTY.sendAsPacket(parser.parseProperty(property));
    }

    public void requestProperties() {
        Packets.SEND_PROPERTY.sendAsPacket();
    }

    public String getProperty(UserProperties userProperties) {
        return properties.getOrDefault(userProperties,"NotFound");
    }

    public void setProperty(UserProperties property, String value) {
        if (properties.containsKey(property)) {
            properties.replace(property, value);
        } else {
            properties.put(property, value);
        }
    }

    public void setAndSendProperty(UserProperties property, String value) {
        setProperty(property, value);
        sendProperty(property);
    }

    public String senderName() {
        return senderName;
    }

    public Parser parser() {
        return parser;
    }

    public PlayerCosmetics cosmetics() {
        return cosmetics;
    }

    public IRC irc() {
        return irc;
    }

    public static User createUser(IRC irc, String name, String clientName, String hwid, String server) {
        User user = new User(irc);
        user.senderName = name;
        user.properties.put(UserProperties.INGAME_NAME, name);
        user.properties.put(UserProperties.CLIENT, clientName);
        user.properties.put(UserProperties.HWID, hwid);
        user.properties.put(UserProperties.SERVER, server);
        return user;
    }
}
