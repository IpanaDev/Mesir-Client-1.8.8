package wolfirc.lex.impl;

import wolfirc.IRCChannel;
import wolfirc.IRCUser;
import wolfirc.WolfIRC;
import wolfirc.lex.Lex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserNickChangeLex extends Lex {

	public UserNickChangeLex() {
		super(Pattern.compile("^:([^ ]+)!([^ ]+) NICK :([^ ]+)$"));
	}

	@Override
	public void onDataReceive(String data, Matcher matcher, WolfIRC client) {
		final String oldName = matcher.group(2);
		final String newName = matcher.group(3);

		for (IRCChannel ircChannel : client.joinedRooms.values()) {
			for (IRCUser user : ircChannel.users()) {
				if (user.username().equalsIgnoreCase(oldName)) {
					user.setUsername(newName);
					break;
				}
			}
		}
		client.onUserNickChangeEvent.fire(oldName, newName);
	}
}
