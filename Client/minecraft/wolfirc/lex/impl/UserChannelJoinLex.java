package wolfirc.lex.impl;

import wolfirc.IRCChannel;
import wolfirc.IRCUser;
import wolfirc.WolfIRC;
import wolfirc.lex.Lex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserChannelJoinLex extends Lex {

	public UserChannelJoinLex() {
		super(Pattern.compile("^:([^! ]+)![^ :]+ JOIN :#([^ :]+)$"));
	}

	@Override
	public void onDataReceive(String data, Matcher matcher, WolfIRC client) {
		final String username = matcher.group(1);
		final String channelName = matcher.group(2);
		final IRCChannel ircChannel = client.joinedRooms.computeIfAbsent(channelName, f -> new IRCChannel(channelName));

		final IRCUser user = new IRCUser(username);
		if (user.username().equalsIgnoreCase(client.username())) {
			ircChannel.users().add(user);
			client.onChannelJoinEvent.fire(channelName);
		} else {
			ircChannel.users().add(user);
			client.onUserJoinChannelEvent.fire(ircChannel, user);
		}
	}
}
