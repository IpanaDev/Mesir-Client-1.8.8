package wolfirc.events;

import pisi.unitedmeows.yystal.clazz.delegate;

public interface WOnUserLeave extends delegate {
    void onUserLeave(String username, String channel);
}

