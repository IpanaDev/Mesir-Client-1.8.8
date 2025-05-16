package ipana.events;

import pisi.unitedmeows.eventapi.event.Event;

public class EventCamera extends Event {
    private double x,y,z;
    private float yaw,pitch;
    private float partialTicks;
    private Type type;

    public EventCamera(double x, double y, double z, float yaw, float pitch,float partialTicks) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.partialTicks = partialTicks;
        this.type = Type.PRE;
    }
    public EventCamera(float partialTicks) {
        this.partialTicks = partialTicks;
        this.type = Type.POST;
    }
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        PRE,POST
    }
}
