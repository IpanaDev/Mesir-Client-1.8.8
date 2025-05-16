package ipana.renders.settings.anticheat;

public class Check {
    private String name;
    private boolean enabled;

    public Check() {
        name = getClass().getSimpleName();
        enabled = true;
    }

    public String name() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean _enabled) {
        enabled = _enabled;
    }


}
