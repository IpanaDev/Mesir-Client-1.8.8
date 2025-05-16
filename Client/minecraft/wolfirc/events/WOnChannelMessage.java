package wolfirc.events;

import pisi.unitedmeows.yystal.clazz.delegate;
import wolfirc.IRCChannel;
import wolfirc.IRCUser;

public interface WOnChannelMessage extends delegate {
	public void onChannelMessage(IRCChannel channel, IRCUser sender, String message);
}
