package ipana.modules.movement.modes.fly;

import ipana.events.*;
import ipana.managements.module.Module;
import ipana.managements.value.Mode;
import ipana.modules.movement.Fly;

public class FlyMode extends Mode<Fly> {
    public FlyMode(String name, Fly parent) {
        super(name, parent);
    }

    public void onMove(EventMoveInput event) {

    }
    public void onMove(EventMoving event) {

    }
    public void onReceive(EventPacketReceive event) {

    }
    public void onPre(EventPreUpdate event) {

    }
    public void onPost(EventPostUpdate event) {

    }
    public void onSetBack(EventSetBack event) {

    }
    public void onMouse(EventMouse event) {

    }
    public void onDisable() {

    }
    public void onEnable() {

    }

    public Object getSuffix() {
        return null;
    }


}
