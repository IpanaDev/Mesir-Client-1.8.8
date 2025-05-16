package ipana.irc;

import wolfirc.WolfIRC;

public class NativeIRC {
    private static final int PORT = 6667;
    private static final String NODE = "chat.freenode.net";

    private static WolfIRC ircClient;

    public static void connect(String name) {
        ircClient = new WolfIRC(name, "Mesir Client");
        ircClient.connect(NODE, PORT);
    }

    public static WolfIRC ircClient() {
        return ircClient;
    }
}
