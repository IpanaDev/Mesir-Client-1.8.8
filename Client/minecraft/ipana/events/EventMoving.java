package ipana.events;

import ipana.utils.player.PlayerUtils;
import pisi.unitedmeows.eventapi.event.Event;

import static ipana.utils.player.PlayerUtils.calculate;

public class EventMoving extends Event {
    private double x;
    private double y;
    private double z;

    public EventMoving(double x,double y,double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public void setSpeed(double speed) {
        double[] c = calculate(speed);
        setX(c[0]);
        setZ(c[1]);
    }
}
