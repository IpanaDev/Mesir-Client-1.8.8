package ipana.modules.player.modes.autopot;

import ipana.events.EventMoving;
import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.managements.value.Mode;
import ipana.modules.player.AutoPot;

public class AutoPotMode extends Mode<AutoPot> {
    public AutoPotMode(String name, AutoPot parent) {
        super(name, parent);
    }

    public void onEnable() {}
    public void onMove(EventMoving event) {}
    public void onPre(EventPreUpdate event) {}
    public void onPost(EventPostUpdate event) {}
}
