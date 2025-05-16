package ipana.irc;

import ipana.Ipana;
import ipana.eventapi.EventManager;
import ipana.events.EventTick;
import ipana.irc.packet.Packets;
import ipana.irc.user.User;
import ipana.irc.user.UserProperties;
import ipana.irc.packet.Packet;
import ipana.irc.packet.PacketManager;
import ipana.renders.ingame.IRChat;
import ipana.renders.ingame.Line;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiNewChat;
import pisi.unitedmeows.eventapi.event.listener.Listener;
import wolfirc.WolfIRC;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IRC {
    private final String CHANNEL;
    private final String channelSuffix;
    private List<User> users = new ArrayList<>();
    public User NULL_USER;
    private User self;
    public int tick;
    private WolfIRC ircClient;
    private String name;
    private GuiNewChat ircChat;

    public IRC(String channel) {
        CHANNEL = channel;
        this.channelSuffix = Ipana.connectedIRCs().isEmpty() ? "#Main" : "#"+CHANNEL;
        ircClient = NativeIRC.ircClient();
    }
    public void startIRC() throws IOException, NoSuchAlgorithmException {
        startIRC(Minecraft.getMinecraft().getSession().getUsername());
    }
    public void startIRC(String userName) throws IOException, NoSuchAlgorithmException {
        name = userName;
        ircChat = new GuiNewChat(channelSuffix, Minecraft.getMinecraft());
        Ipana.addNewChat(ircChat);
        if (isMain()) {
            ircClient.onMotdEndEvent.bind(() -> ircClient.joinChannel(CHANNEL));
        } else {
            ircClient.joinChannel(CHANNEL);
        }

        ircClient.onUserLeaveEvent.bind((user, channel) -> {
            if (channel.equals(channel())) {
                final User sender = IRC.this.getSender(user);
                ircChat.printChatMessage(Line.create(sender.senderName(), "left.", sender));
                users.remove(sender);
                ircChat.messagesUnseen++;
            }
        });

        ircClient.onUserQuitEvent.bind((user, reason) -> {
            final User sender = IRC.this.getSender(user, false);
            if (sender != null) {
                ircChat.printChatMessage(Line.create(sender.senderName(), "disconnected (" + reason + ").", sender));
                users.remove(sender);
                ircChat.messagesUnseen++;
            }
        });

        ircClient.onChannelMessage.bind((channel, ircUser, message) -> {
            if (channel.name().equals(channel())) {
                String username = ircUser.username();
                final User user = IRC.this.getSender(username);
                if (isMain() && message.startsWith("$")) {
                    final String[] msg = message.substring(1).split(" ");
                    final String packetName = msg[0];
                    for (final Packet packet : PacketManager.packets()) {
                        if (packet.name().equals(packetName)) {
                            if (packet.hasArgs()) {
                                final String[] args = user.parser().parseArgs(msg[1]);
                                packet.execute(user, args);
                            } else {
                                packet.execute(user);
                            }
                            break;
                        }
                    }
                } else {
                    ircChat.printChatMessage(Line.create(username, message, user));
                    ircChat.messagesUnseen++;
                }
            }
        });

        NULL_USER = User.createUser(this, "null_user", "null_user", "none", "none");
        self = User.createUser(this, name, Ipana.clientName, Ipana.getHWID(), "Title");
        if (isMain()) {
            ircClient.onChannelJoinEvent.bind(channel -> {
                if (channel.equals(channel())) {
                    self.requestOnJoin();
                }
            });
        } else {
            ircClient.onUserJoinChannelEvent.bind((channel, user) -> {
               if (channel.name().equals(channel())) {
                   ircChat.printChatMessage(Line.create(user.username(), "connected.", getSender(user.username())));
                   ircChat.messagesUnseen++;
               }
            });
        }
        PacketManager.addPackets();
        EventManager.eventSystem.subscribeAll(this);
    }

    public Listener<EventTick> event = new Listener<>((EventTick event) -> {
        tick++;
        if (tick % 1200 == 0 && self().cosmetics() != null && isMain()) {
            self().cosmetics().sendPacket();
        }
    });

    private User getSender(String sender) {
        return getSender(sender, true);
    }
    private User getSender(String sender, boolean create) {
        for (User user : users) {
            if (user.senderName().equals(sender)) {
                return user;
            }
        }
        if (create) {
            User user = User.createUser(this, sender, "", "", "");
            users.add(user);
            return user;
        } else {
            return null;
        }
    }
    public User getUser(String ingame) {
        if (ingame.equals(Minecraft.getMinecraft().getSession().getUsername())) return self();
        Optional<User> findAny = users.stream().filter(user -> user.getProperty(UserProperties.INGAME_NAME).equals(ingame)).findFirst();
        return findAny.orElse(NULL_USER);
    }

    public void leave() {
        Packets.LEAVE_PACKET.sendAsPacket();
        ircClient.leaveChannel(channel(), "User Quit");
    }

    public List<User> users() {
        return users;
    }

    public User self() {
        return self;
    }

    public void msg(String string) {
        ircClient.sendMessage(string, CHANNEL);
    }

    public GuiNewChat ircChat() {
        return ircChat;
    }

    public String channel() {
        return CHANNEL;
    }

    public String getName() {
        return name;
    }

    private boolean isMain() {
        return this == Ipana.mainIRC();
    }
}
