package ipana.renders.settings.anticheat;

import ipana.utils.ncp.handler.Handler;

public class AntiCheat<A extends Handler> {
    private String name;
    private Check[] checks;
    private boolean active;
    private A handler;

    public AntiCheat(String name, A handler, Check... checks) {
        this.name = name;
        this.handler = handler;
        this.checks = checks;
    }

    public String name() {
        return name;
    }

    public Check[] checks() {
        return checks;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean _active) {
        if (_active && !active) {
            handler.register();
        } else if (!_active && active) {
            handler.unregister();
        }
        active = _active;
    }
}
