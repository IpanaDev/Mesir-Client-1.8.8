package wolfirc.lex.impl;

import wolfirc.WolfIRC;
import wolfirc.lex.Lex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PingLex extends Lex {

    public PingLex() {
        super(Pattern.compile("(PING\\ :)([^ :\\n]+)(:?)"));
    }

    @Override
    public void onDataReceive(String data, Matcher matcher, WolfIRC client) {
        String pingMessage = matcher.group(2);
        client.sendRawLine("PONG :" + pingMessage);
        client.onPingEvent.fire(pingMessage);
    }
}
