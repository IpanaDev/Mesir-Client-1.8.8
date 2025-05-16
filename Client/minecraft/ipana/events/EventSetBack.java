package ipana.events;

import pisi.unitedmeows.eventapi.event.Event;

public class EventSetBack extends Event {
    private double xOff,yOff,zOff;
    private float yaw, pitch;
    private State state;
    private boolean cancelPacket;
    private boolean cancelSetPos;
    private boolean resetMotion;

    public EventSetBack(double xOff, double yOff, double zOff, float yaw, float pitch, State state) {
        this.xOff = xOff;
        this.yOff = yOff;
        this.zOff = zOff;
        this.yaw = yaw;
        this.pitch = pitch;
        this.state = state;
        this.resetMotion = true;
    }

    public double xOff() {
        return xOff;
    }

    public double yOff() {
        return yOff;
    }

    public double zOff() {
        return zOff;
    }

    public boolean cancelPacket() {
        return cancelPacket;
    }

    public boolean cancelSetPos() {
        return cancelSetPos;
    }

    public boolean resetMotion() {
        return resetMotion;
    }

    public void setResetMotion(boolean resetMotion) {
        this.resetMotion = resetMotion;
    }

    public void cancelPacket(boolean _cancelPacket) {
        cancelPacket = _cancelPacket;
    }

    public void cancelSetPos(boolean _cancelSetPos) {
        cancelSetPos = _cancelSetPos;
    }

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    public State state() {
        return state;
    }

    public enum State {
        PRE, POST
    }
}
