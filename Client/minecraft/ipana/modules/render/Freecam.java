package ipana.modules.render;

import ipana.events.*;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.NumberValue;
import ipana.utils.player.PlayerUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.lwjgl.input.Keyboard;

import pisi.unitedmeows.eventapi.event.listener.Listener;

public class Freecam extends Module {
    public Freecam() {
        super("Freecam", Keyboard.KEY_NONE, Category.Render, "Eymen moment öyle bir andırki hayatın yamulur");
    }
    private double x,y,z;
    private NumberValue<Double> speed = new NumberValue<>("Speed",this,1.0,0.2,5.0,0.1,"Eymeni allahina kavusturma hizi");

    @Override
    public void onEnable() {
        if (mc.thePlayer != null) {
            x = mc.thePlayer.posX;
            y = mc.thePlayer.posY;
            z = mc.thePlayer.posZ;
            mc.thePlayer.noClip = true;
        }
        super.onEnable();
    }

    private Listener<EventMoving> onMove = new Listener<>(event -> {
        double[] spd = PlayerUtils.calculate(speed.getValue());
        event.setX(spd[0]);
        event.setZ(spd[1]);
        event.setY(mc.thePlayer.motionY = 0);
        if (PlayerUtils.isMoving()) {
            if (mc.thePlayer.moveForward < 0) {
                event.setY((mc.thePlayer.rotationPitch) / 45);
            } else if (mc.thePlayer.moveForward > 0) {
                event.setY((-mc.thePlayer.rotationPitch) / 45);
            }
        }
    });


    private Listener<EventExcuseMeWTF> onBefore = new Listener<>(event -> mc.thePlayer.noClip = true);


    private Listener<EventBoundingBox> onBB = new Listener<>(event -> event.setBoundingBox(null));


    private Listener<EventPreUpdate> onPre = new Listener<>(event -> {
        mc.thePlayer.noClip = true;
        event.setCancelPackets(true);
        PlayerUtils.packet(new C03PacketPlayer.C05PacketPlayerLook(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, mc.thePlayer.onGround));
    });

    @Override
    public void onDisable() {
        mc.thePlayer.setPosition(x,y,z);
        mc.thePlayer.noClip = false;
        mc.renderGlobal.loadRenderers();
        super.onDisable();
    }
}
