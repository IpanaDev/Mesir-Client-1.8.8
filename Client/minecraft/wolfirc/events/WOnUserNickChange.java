package wolfirc.events;

import pisi.unitedmeows.yystal.clazz.delegate;
import wolfirc.IRCUser;

public interface WOnUserNickChange extends delegate {
	public void onUserNickChange(String oldName, String newName);
}
