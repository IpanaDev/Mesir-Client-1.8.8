package ipana.events;

import pisi.unitedmeows.eventapi.event.Event;

public class EventPostUpdate extends Event {
    private EventPreUpdate lastEvent;

    public EventPostUpdate(EventPreUpdate lastEvent) {
        this.lastEvent = lastEvent;
    }

    public EventPreUpdate lastEvent() {
        return lastEvent;
    }
}
