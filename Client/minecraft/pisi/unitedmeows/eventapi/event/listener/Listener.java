package pisi.unitedmeows.eventapi.event.listener;


import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.IFunction;
import pisi.unitedmeows.eventapi.event.utils.TypeResolver;

import java.util.function.Predicate;

public class Listener<X extends Event> {
	protected Predicate<X> filter;
	protected IFunction<X> function;
	protected Event.Weight weight;
    protected Class<X> target;

	public Listener(IFunction<X> event) {
		this.function = event;
		this.weight = Event.Weight.MEDIUM;
        this.target = (Class<X>) TypeResolver.resolveRawArgument(IFunction.class, function.getClass());
	}

	public void call(Event event) {
		if (filter != null && !filter.test((X) event)) {
		    return;
		}
        this.function.call((X) event);
	}

	public IFunction<X> function() {
		return function;
	}

    public Class<X> target() {
        return target;
    }

    public Event.Weight getWeight() {
        return weight;
    }

    public Listener<X> filter(Predicate<X> filter) {
		this.filter = filter;
		return this;
	}

	public Listener<X> weight(Event.Weight weight) {
		this.weight = weight;
		return this;
	}
}
