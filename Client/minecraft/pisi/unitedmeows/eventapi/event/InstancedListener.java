package pisi.unitedmeows.eventapi.event;

import pisi.unitedmeows.eventapi.event.listener.Listener;

public record InstancedListener(Object instance, Listener<? extends Event> listener) { }