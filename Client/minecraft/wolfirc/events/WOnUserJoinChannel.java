package wolfirc.events;

import pisi.unitedmeows.yystal.clazz.delegate;
import wolfirc.IRCChannel;
import wolfirc.IRCUser;

public interface WOnUserJoinChannel extends delegate {
	public void onUserJoinChannel(IRCChannel channel, IRCUser user);
}
