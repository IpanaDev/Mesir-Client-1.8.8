package wolfirc.events;

import pisi.unitedmeows.yystal.clazz.delegate;

public interface WOnRawData extends delegate {
    public void onRawData(final String data);
}
