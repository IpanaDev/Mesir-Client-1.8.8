package ipana.modules.movement.modes.fly;

import ipana.events.*;
import ipana.managements.module.Modules;
import ipana.modules.movement.Fly;
import ipana.utils.math.MathUtils;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;

import java.util.List;

import static ipana.utils.player.PlayerUtils.*;

public class OldHighJump extends FlyMode {
    public static final int DAMAGE_COOLDOWN = 11;
    private int ticks;
    private double y;


    public OldHighJump(Fly parent) {
        super("OldHighJump", parent);
    }

    @Override
    public void onEnable() {
        ticks = 0;
        if (mc.thePlayer != null) {
            y = mc.thePlayer.posY;
        }
        //PlayerUtils.debug(lastYDist);
        super.onEnable();
    }

    @Override
    public void onMove(EventMoving event) {
        Fly fly = getParent();
        double speed = getBaseMoveSpeed();
        if (ticks >= 10) {
            double[] d = new double[]{
                    0, 0, 0,
                    -0.05, -0.05, -0.05,
                    -0.15
            };
            if (ticks - 10 < d.length) {
                double v = d[ticks - 10];
                event.setY(mc.thePlayer.motionY = v);
            }
            if (ticks == 10) {
                speed = 1;
            } else if (ticks == 11) {
                speed = 0.6;
            }
            ticks++;
        }
        double finalSpeed = Math.max(getBaseMoveSpeed(), speed-1E-2);
        double[] c = calculate(finalSpeed);
        event.setX(c[0]);
        event.setZ(c[1]);
        super.onMove(event);
    }

    @Override
    public void onMouse(EventMouse event) {
        Fly fly = getParent();
        super.onMouse(event);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        Fly fly = getParent();
        event.setOnGround(true);
    }

    @Override
    public void onPost(EventPostUpdate event) {
        Fly fly = getParent();

        if (mc.thePlayer.onGround && ticks >= 10) {
            mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
            getParent().toggle();
        }
        mc.timer.timerSpeed = 1f;
        if (ticks == 0) {
            double[] d = offsets();
            for (double v : d) {
                mc.thePlayer.setPosition(mc.thePlayer.posX, y + v, mc.thePlayer.posZ);
                sendOffset(0, 0, 0, true);
            }
            mc.timer.timerSpeed = 0.2f;
            ticks = 10;
        }
        super.onPost(event);
    }

    private double[] offsets() {
        double[] d = new double[10];
        double gn = 1E-7;
        double startY = 1.35-gn;
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            startY += 0.6 + mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier();
        }
        double nextY = startY;
        double bypass = 0.6-gn;
        for (int i = 0; i < d.length; i++) {
            int tick = i % 3;
            if (tick == 0) {
                d[i] = nextY;
            } else if (tick == 1) {
                d[i] = nextY - (nextY - bypass) / 2 - gn;
            } else {
                d[i] = d[i - 1] - (nextY - bypass) / 2 + gn;
                nextY = startY + nextY - (nextY - bypass) / 2 - gn;
            }
        }
        return d;
    }
}
