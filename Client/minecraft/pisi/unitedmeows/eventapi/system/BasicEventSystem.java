package pisi.unitedmeows.eventapi.system;

import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.InstancedListener;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class BasicEventSystem implements IEventSystem {
    private HashMap<Class<? extends Event>, CopyOnWriteArrayList<InstancedListener>> LOOKUP = new HashMap<>();

	@Override
	public void subscribeAll(Object instance) {
		/* find field from class */
		for (Field field : instance.getClass().getDeclaredFields()) {
			if (Listener.class.isAssignableFrom(field.getType())) {

				/* make field accessible */
				if (!field.isAccessible()) {
					field.setAccessible(true);
				}

				try {
					/* subscribe listener */
					subscribe(instance, (Listener<?>) field.get(instance));
				} catch (IllegalAccessException e) {
                    e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void subscribeAll(Object instance, Listener<?>... listeners) {
        for (var listener : listeners) {
            subscribe(instance, listener);
        }
	}

	@Override
	public void subscribe(Object instance, Listener<?> listener) {
        LOOKUP.computeIfAbsent(listener.target(), _ -> new CopyOnWriteArrayList<>());
        var listeners = LOOKUP.get(listener.target());
        listeners.add(new InstancedListener(instance, listener));
        listeners.sort(Comparator.comparingInt(l -> -l.listener().getWeight().value()));
	}

	@Override
	public void unsubscribe(Listener<?> listener) {
        for (var entry : LOOKUP.entrySet()) {
            entry.getValue().removeIf(x -> x.listener() == listener);
        }
	}

	@Override
	public void unsubscribeAll(Object instance) {
        for (var entry : LOOKUP.entrySet()) {
            entry.getValue().removeIf(x -> x.instance() == instance);
        }
	}

	@Override
    public void fire(Event event) {
        var listeners = LOOKUP.get(event.getClass());
        if (listeners == null || listeners.isEmpty()) {
            return;
        }
        for (var instancedListener : listeners) {
            instancedListener.listener().call(event);

            if (event.isStopped()) {
                break;
            }
        }
    }
}
