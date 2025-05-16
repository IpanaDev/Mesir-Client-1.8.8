package wolfirc.lex.impl;

import wolfirc.IRCChannel;
import wolfirc.IRCUser;
import wolfirc.WolfIRC;
import wolfirc.lex.Lex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChannelMessageLex extends Lex {

	public ChannelMessageLex() {
		super(Pattern.compile("^(:)([^ :!]+)[^ :]+ PRIVMSG #([^ :]+) :(.+)"));
	}

	@Override
	public void onDataReceive(String data, Matcher matcher, WolfIRC client) {
		final String sender = matcher.group(2);
		final String channel = matcher.group(3);
		final String message = matcher.group(4);

		IRCUser ircUser = null;
		final IRCChannel ircChannel = client.joinedRooms.computeIfAbsent(channel, f -> new IRCChannel(channel));
		for (IRCUser user : ircChannel.users()) {
			if (user.username().equalsIgnoreCase(sender)) {
				ircUser = user;
			}
		}
		if (ircUser == null) {
			ircUser = new IRCUser(sender);
			ircChannel.users().add(ircUser);
		}

		client.onChannelMessage.fire(ircChannel, ircUser, message);
	}
}
