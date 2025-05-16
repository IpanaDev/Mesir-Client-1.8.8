package wolfirc.events;

import pisi.unitedmeows.yystal.clazz.delegate;
import pisi.unitedmeows.yystal.clazz.ref;

public interface WOnPing extends delegate {
    public void onPing(String pingMessage, ref<Boolean> shouldPong);
}
