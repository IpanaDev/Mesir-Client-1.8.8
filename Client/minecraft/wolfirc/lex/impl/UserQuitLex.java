package wolfirc.lex.impl;

import wolfirc.IRCChannel;
import wolfirc.IRCUser;
import wolfirc.WolfIRC;
import wolfirc.lex.Lex;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserQuitLex extends Lex {

	public UserQuitLex() {
		super(Pattern.compile("^:([^ :!]+)!([^ ]+) QUIT :(.+)$"));
	}

	@Override
	public void onDataReceive(String data, Matcher matcher, WolfIRC client) {
		String quitUser = matcher.group(1);
		String reason = matcher.group(3);

		client.onUserQuitEvent.fire(quitUser, reason);

		for (IRCChannel channel : client.joinedRooms.values()) {
			channel.users().removeIf(ircUser -> ircUser.username().equalsIgnoreCase(quitUser));
		}
	}
}
