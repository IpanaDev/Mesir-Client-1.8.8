package ipana.events;

import pisi.unitedmeows.eventapi.event.Event;

public class EventMouse extends Event {
    private int key;

    public EventMouse(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
