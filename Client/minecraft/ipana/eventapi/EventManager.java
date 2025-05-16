package ipana.eventapi;

import ipana.events.*;
import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.system.BasicEventSystem;

import java.util.ArrayList;

public class EventManager {
    public static BasicEventSystem eventSystem = new BasicEventSystem();
    private static ArrayList<Class<? extends Event>> events = new ArrayList<>();

    static {
        events.add(EventKey.class);
        events.add(EventMouse.class);
        events.add(EventMoveInput.class);
        events.add(EventMoving.class);
        events.add(EventPacketReceive.class);
        events.add(EventPacketSend.class);
        events.add(EventPreUpdate.class);
        events.add(EventPostUpdate.class);
        events.add(EventRender2D.class);
        events.add(EventRender3D.class);
    }

    public static ArrayList<Class<? extends Event>> events() {
        return events;
    }
}