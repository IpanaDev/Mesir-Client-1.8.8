package wolfirc.events;

import pisi.unitedmeows.yystal.clazz.delegate;

public interface WOnUserQuit extends delegate {
    void onUserQuit(String username, String reason);
}
