package ipana.modules.movement.modes.speed;

import ipana.events.*;
import ipana.managements.module.Module;
import ipana.managements.value.Mode;
import ipana.modules.movement.Speed;

public class SpeedMode extends Mode<Speed> {
    public SpeedMode(String name, Speed parent) {
        super(name, parent);
    }
    public void onMoving(EventMoving event) {

    }
    public void onTick(EventTick event) {

    }
    public void onMove(EventMoveInput event) {

    }
    public void onPre(EventPreUpdate event) {

    }
    public void onPost(EventPostUpdate event) {

    }
    public void onStep(EventStep event) {

    }
    public void onFrame(EventFrame event) {

    }
    public void onBeforeUpdate(EventExcuseMeWTF event) {

    }
    public void onReceive(EventPacketReceive event) {

    }
    public void onSend(EventPacketSend event) {

    }
    public void onSetBack(EventSetBack event) {

    }
    public double nextSpeed() {
        return 0;
    }
    public double nextY() {
        return 0;
    }
    public void onEnable() {

    }
    public void onDisable() {

    }
}
