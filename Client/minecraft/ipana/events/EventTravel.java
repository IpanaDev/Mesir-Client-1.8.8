package ipana.events;

import pisi.unitedmeows.eventapi.event.Event;

public class EventTravel extends Event {
    private float forward, strafe, yaw;

    public EventTravel(float forward, float strafe, float yaw) {
        this.forward = forward;
        this.strafe = strafe;
        this.yaw = yaw;
    }

    public float forward() {
        return forward;
    }

    public float strafe() {
        return strafe;
    }


    public float yaw() {
        return yaw;
    }

    public void setForward(float forward) {
        this.forward = forward;
    }

    public void setStrafe(float strafe) {
        this.strafe = strafe;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }
}
