package wolfirc.lex.impl;

import wolfirc.IRCChannel;
import wolfirc.IRCUser;
import wolfirc.WolfIRC;
import wolfirc.lex.Lex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelNamesLex extends Lex {

	public ChannelNamesLex() {
		super(Pattern.compile("^(:)([^ :]+) 353 ([^ :])+ = #([^ :]+) :(.+)$"));
	}

	@Override
	public void onDataReceive(String data, Matcher matcher, WolfIRC client) {
		final String users = matcher.group(5).trim();
		final String channel = matcher.group(4);
		final IRCChannel ircChannel = client.joinedRooms.computeIfAbsent(channel, f -> new IRCChannel(channel));
		if (users.contains(" ")) {
			for (String user : users.split(" ")) {
				ircChannel.addUser(new IRCUser(user));
			}
		} else {
			ircChannel.addUser(new IRCUser(users));
		}
	}
}
