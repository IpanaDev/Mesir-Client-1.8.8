package ipana.events;

import pisi.unitedmeows.eventapi.event.Event;

public class EventFrame extends Event {
    private boolean isOnTick;

    public EventFrame(boolean isOnTick) {
        this.isOnTick = isOnTick;
    }

    public boolean isOnTick() {
        return isOnTick;
    }
}
