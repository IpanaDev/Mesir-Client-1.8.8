package ipana.modules.render;

import net.minecraft.util.MathHelper;
import ipana.events.*;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.Value;
import ipana.managements.value.values.BoolValue;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.lwjgl.input.Keyboard;

import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.awt.*;

public class Camera extends Module {
    private BoolValue followPlayer = new BoolValue("FollowPlayer",this,false,"Camera follow the player");
    public Camera() {
        super("Camera", Keyboard.KEY_NONE,Category.Render,"Camera!");
    }
    private double forward,strafe;
    private double startX,startY,startZ;
    private double moveX,moveY,moveZ;
    public double x,y,z;
    public float yaw,pitch;
    private boolean enabled;
    private double lX,lY,lZ;
    public boolean listening;

    @Override
    public void onEnable() {
        if (mc.thePlayer != null) {
            startX = mc.thePlayer.posX;
            startY = mc.thePlayer.posY;
            startZ = mc.thePlayer.posZ;
        }
        forward=0;
        strafe=0;
        listening = true;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mc.thePlayer.setPosition(startX,startY,startZ);
        moveX=0;
        moveY=0;
        moveZ=0;
        super.onDisable();
    }
    private Listener<EventCamera> onCamera = new Listener<EventCamera>(event -> {
        if (!followPlayer.getValue() || listening) {
            double d0 = (mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * (double) event.getPartialTicks());
            double d1 = (mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * (double) event.getPartialTicks());
            double d2 = (mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * (double) event.getPartialTicks());
            x = ((d0 - startX) + moveX);
            y = ((d1 - startY) + moveY);
            z = ((d2 - startZ) + moveZ);
        }
        event.setX(x);
        event.setY(y);
        event.setZ(z);
        event.setYaw(yaw);
        event.setPitch(pitch);
    }).filter(event -> event.getType() == EventCamera.Type.PRE);

    private Listener<EventPreUpdate> onPre = new Listener<>(event -> {
        if (mc.gameSettings.keyBindForward.pressed) {
            forward=-0.98;
        } else if (mc.gameSettings.keyBindBack.pressed) {
            forward=0.98;
        } else {
            forward = 0;
        }
        if (mc.gameSettings.keyBindRight.pressed) {
            strafe=0.98;
        } else if (mc.gameSettings.keyBindLeft.pressed) {
            strafe=-0.98;
        } else {
            strafe = 0;
        }
        double baseCameraSpeed = 0.5;
        if (listening) {
            moveX+=calculate(baseCameraSpeed)[0];
            if (mc.gameSettings.keyBindJump.pressed) {
                moveY-=baseCameraSpeed;
            } else if (mc.gameSettings.keyBindSneak.pressed) {
                moveY+=baseCameraSpeed;
            }
            moveZ+=calculate(baseCameraSpeed)[1];
            yaw = mc.thePlayer.rotationYaw;
            pitch = mc.thePlayer.rotationPitch;
        }
    });
    private Listener<EventRender2D> on2D = new Listener<EventRender2D>(event -> {
        ScaledResolution sr = RenderUtils.SCALED_RES;
        String str = "Listening";
        int width = mc.fontRendererObj.getStringWidth(str) / 2;
        int x = sr.getScaledWidth() / 2 - width / 2;
        mc.fontRendererObj.drawStringWithShadow(str, x, 1, Color.white.getRGB());
    }).filter(filter -> listening);

    private Listener<EventKey> onKey = new Listener<EventKey>(event -> {
        listening=!listening;
    }).filter(filter -> filter.getKey() == Keyboard.KEY_RETURN);

    private double[] calculate(double speed) {
        double forward = this.forward;
        double strafe = this.strafe;
        float yaw = mc.thePlayer.rotationYaw+180;
        if (forward != 0.0D)
        {
            if (strafe > 0.0D) {
                yaw += (forward > 0.0D ? -45 : 45);
            } else if (strafe < 0.0D) {
                yaw += (forward > 0.0D ? 45 : -45);
            }
            strafe = 0.0D;
            if (forward > 0.0D) {
                forward = 1.0D;
            } else if (forward < 0.0D) {
                forward = -1.0D;
            }
        }

        double xSpeed = forward * speed * Math.cos(Math.toRadians(yaw + 90.0F)) + strafe * speed * MathHelper.sin(Math.toRadians(yaw + 90.0F));
        double zSpeed = forward * speed * MathHelper.sin(Math.toRadians(yaw + 90.0F)) - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F));
        return new double[]{xSpeed,zSpeed};
    }
}
