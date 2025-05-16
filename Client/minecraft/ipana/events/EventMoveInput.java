package ipana.events;

import pisi.unitedmeows.eventapi.event.Event;

public class EventMoveInput extends Event {
    private float forward,strafe;
    private boolean jumping;
    private boolean sneaking;

    public EventMoveInput(float forward, float strafe,boolean jumping) {
        this.forward = forward;
        this.strafe = strafe;
        this.jumping = jumping;
    }
    public EventMoveInput(boolean sneaking) {
        this.sneaking = sneaking;
    }

    public float getForward() {
        return forward;
    }

    public void setForward(float forward) {
        this.forward = forward;
    }

    public float getStrafe() {
        return strafe;
    }

    public void setStrafe(float strafe) {
        this.strafe = strafe;
    }

    public boolean isSneaking() {
        return sneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.sneaking = sneaking;
    }

    public boolean isJumping() {
        return jumping;
    }

    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }
}
