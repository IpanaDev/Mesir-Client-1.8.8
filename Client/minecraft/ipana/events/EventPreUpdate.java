package ipana.events;

import ipana.utils.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C03PacketPlayer;
import pisi.unitedmeows.eventapi.event.Event;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;

public class EventPreUpdate extends Event{
    private double x,y,z;
    private float yaw,pitch;
    private boolean onGround;
    private boolean cancelPackets;
    private C03PacketPlayer packet;
    private UpdateType type;

    public EventPreUpdate(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
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

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }


    public boolean isCancelPackets() {
        return cancelPackets;
    }

    public void setCancelPackets(boolean cancelPackets) {
        this.cancelPackets = cancelPackets;
    }

    public C03PacketPlayer getPacket() {
        return packet;
    }

    public void setPacket(C03PacketPlayer packet) {
        this.packet = packet;
    }

    public void setType(UpdateType _type) {
        type = _type;
    }

    public UpdateType type() {
        return type;
    }

    public void sendLastPacket(EventPreUpdate lastEvent) {
        lastEvent.getPacket().yaw = this.getYaw();
        lastEvent.getPacket().pitch = this.getPitch();

        Minecraft.getMinecraft().thePlayer.report(this.getYaw(), this.getPitch());
        PlayerUtils.packet(lastEvent.getPacket());
        //sendLastPacket = true;
    }

    public enum UpdateType {
        MOVE,LOOK,MOVE_LOOK,GROUND
    }
}
