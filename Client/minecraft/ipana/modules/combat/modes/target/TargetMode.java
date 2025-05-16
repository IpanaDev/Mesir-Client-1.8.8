package ipana.modules.combat.modes.target;

import ipana.events.EventMoving;
import ipana.events.EventPreUpdate;
import ipana.managements.value.Mode;
import ipana.modules.combat.Target;

public class TargetMode extends Mode<Target> {

    public TargetMode(String name, Target parent) {
        super(name, parent);
    }

    public void onPre(EventPreUpdate event) {

    }

    public void onMove(EventMoving event) {

    }

    public boolean check() {
        return false;
    }
}