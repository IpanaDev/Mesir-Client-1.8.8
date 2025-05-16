package wolfirc;

import java.util.ArrayList;
import java.util.List;

public class IRCChannel {

	protected String name;
	private List<IRCUser> users;

	public IRCChannel(String _name) {
		name = _name;
		users = new ArrayList<>();
	}

	public void sendMessage(WolfIRC client, String message) {
		client.sendRawLine(String.format("PRIVMSG #%s :%s", name, message));
	}

	public void leave(WolfIRC client, String reason) {
		client.leaveChannel(name, reason);
	}

	public void leave(WolfIRC client) {
		client.leaveChannel(name);
	}

	public void addUser(IRCUser user) {
		boolean exists = false;
		for (IRCUser loopUser : users()) {
			if (user == loopUser || loopUser.username.equalsIgnoreCase(user.username)) {
				exists = true;
				break;
			}
		}
		if (!exists) {
			users().add(user);
		}
	}

	public List<IRCUser> users() {
		return users;
	}

	public String name() {
		return name;
	}
}
