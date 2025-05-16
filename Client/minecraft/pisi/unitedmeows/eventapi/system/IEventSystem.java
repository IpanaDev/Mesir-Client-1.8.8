package pisi.unitedmeows.eventapi.system;

import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.lang.reflect.Field;

public interface IEventSystem {

	void subscribeAll(Object instance);
	void subscribeAll(Object instance, Listener<?>...  listeners);
	void unsubscribeAll(Object instance);

	void subscribe(Object instance, Listener<?> listener);
	void unsubscribe(Listener<?> listener);

	void fire(Event event);
}
