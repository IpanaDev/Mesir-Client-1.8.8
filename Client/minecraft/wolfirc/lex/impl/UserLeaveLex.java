package wolfirc.lex.impl;

import wolfirc.IRCChannel;
import wolfirc.WolfIRC;
import wolfirc.lex.Lex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserLeaveLex extends Lex {
    public UserLeaveLex() {
        super(Pattern.compile("^:([^! ]+)![^ :]+ PART :#([^ :]+)$"));
    }

    @Override
    public void onDataReceive(String data, Matcher matcher, WolfIRC client) {
        final String sender = matcher.group(1);
        final String channel = matcher.group(2);

        client.onUserLeaveEvent.fire(sender, channel);
        client.joinedRooms.get(channel).users().removeIf(ircUser -> ircUser.username().equalsIgnoreCase(sender));
    }
}
