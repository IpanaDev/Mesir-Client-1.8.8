package wolfirc.events;

import pisi.unitedmeows.yystal.clazz.delegate;

public interface WOnChannelJoin extends delegate {
	public void onRoomJoin(final String room);
}
