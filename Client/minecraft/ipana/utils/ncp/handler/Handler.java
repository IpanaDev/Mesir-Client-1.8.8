package ipana.utils.ncp.handler;

import ipana.eventapi.EventManager;

public class Handler {

    public void register() {
        EventManager.eventSystem.subscribeAll(this);
    }

    public void unregister() {
        EventManager.eventSystem.unsubscribeAll(this);
    }
}
