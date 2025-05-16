package ipana.modules.movement;

import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.events.EventStep;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.modules.movement.modes.speed.Fantasy;
import ipana.modules.movement.modes.speed.OldNCP;
import ipana.utils.player.PlayerUtils;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;

public class Step extends Module {
    public Step() {
        super("Step", Keyboard.KEY_NONE,Category.Movement,"Fast step.");
    }
    private Mode mode;
    public boolean timeSlowed;
    private int sentPackets;
    private AxisAlignedBB steppedBB;

    private Listener<EventPreUpdate> onPre = new Listener<EventPreUpdate>(event -> {
        decideMode();
        if (mode == Mode.PRE) {
            steppedBB = null;
            double[] calc = calc();
            mc.thePlayer.stepHeight = (float) calc[calc.length - 1];
            double yDiff = mc.thePlayer.posY - mc.thePlayer.prevPosY;
            if (yDiff > 0.6 && yDiff <= mc.thePlayer.stepHeight && mc.thePlayer.onGround) {
                if (yDiff == 1) {
                    PlayerUtils.send(mc.thePlayer.prevPosX, mc.thePlayer.prevPosY + 0.42, mc.thePlayer.prevPosZ);
                    sentPackets++;
                /*TROOOOOOOOOOOOOL
                event.setY(event.getY()+0.07);
                mc.thePlayer.expandPos(0, 0.07,0);
                */
                } else {
                    for (double yValues : calc) {
                        if (yValues >= yDiff) {
                            break;
                        }
                        PlayerUtils.send(mc.thePlayer.prevPosX, mc.thePlayer.prevPosY + yValues, mc.thePlayer.prevPosZ);
                        sentPackets++;
                    }
                }
            }
        }
    }).filter(filter -> !Modules.PHASE.isEnabled());

    private void decideMode() {
        Speed speed = Modules.SPEED;
        boolean speedCheck = speed.isEnabled() && PlayerUtils.isMoving2() && (speed.mode.getValue() instanceof OldNCP || speed.mode.getValue() instanceof Fantasy);
        mode = Mode.PRE;
    }

    private void resetSpeedTick() {
        Speed speed = Modules.SPEED;
        if (speed.mode.getValue() instanceof OldNCP) {
            speed.ticks = 0;
        } else if (speed.mode.getValue() instanceof Fantasy) {
            speed.ticks = 0;
        }
    }

    private Listener<EventStep> onStep = new Listener<EventStep>(event -> {
        steppedBB = event.postBB();
    }).filter(filter -> !Modules.PHASE.isEnabled());

    private Listener<EventPostUpdate> onPost = new Listener<EventPostUpdate>(event -> {
        if (mode == Mode.POST && steppedBB != null) {
            double[] calc = calc();
            double yDiff = steppedBB.minY-mc.thePlayer.posY;
            if (yDiff > 0.6 && mc.thePlayer.onGround) {
                if (yDiff == 1) {
                    PlayerUtils.sendOffset(0, 0.42, 0);
                    sentPackets++;
                } else {
                    for (double yValues : calc) {
                        if (yValues >= yDiff) {
                            break;
                        }
                        PlayerUtils.sendOffset(0, yValues, 0);
                        sentPackets++;
                    }
                }
            }
            resetSpeedTick();
            mc.thePlayer.motionY = 0;
            double hAllowedDistance = PlayerUtils.baseSpeed311(0,0);
            double[] hs = PlayerUtils.calculate(hAllowedDistance-0.2);
            mc.thePlayer.expandPos(hs[0], yDiff, hs[1]);
            PlayerUtils.sendOffset(0, 0, 0);
            sentPackets++;
            steppedBB = null;
        }
        if (timeSlowed) {
            mc.timer.timerSpeed = 1f;
            timeSlowed = false;
        }
        if (sentPackets > 0) {
            mc.timer.timerSpeed = 1f/(sentPackets+1);
            sentPackets = 0;
            timeSlowed = true;
        }
    }).weight(Event.Weight.LOW).filter(filter -> !Modules.PHASE.isEnabled());

    private double[] calc() {
        double[] array = new double[10];
        double gn = 1E-7;
        double startY = 1.35-gn;
        if (mc.thePlayer.isPotionActive(Potion.jump)) {
            startY += 0.6 + mc.thePlayer.getActivePotionEffect(Potion.jump).getAmplifier();
        }
        double nextY = startY;
        double bypass = 0.6-gn;
        for (int i = 0; i < array.length; i++) {
            int tick = i % 3;
            if (tick == 0) {
                array[i] = nextY;
            } else if (tick == 1) {
                array[i] = nextY - (nextY - bypass) / 2 - gn;
            } else {
                array[i] = array[i - 1] - (nextY - bypass) / 2 + gn;
                nextY = startY + nextY - (nextY - bypass) / 2 - gn;
            }
        }
        return array;
    }


    @Override
    public void onDisable() {
        mc.thePlayer.stepHeight = 0.6f;
        mc.timer.timerSpeed = 1.0f;
        timeSlowed = false;
        super.onDisable();
    }

    @Override
    public void onEnable() {
        timeSlowed = false;
        mc.timer.timerSpeed = 1.0f;
        super.onEnable();
    }

    public boolean isPost() {
        return mode == Mode.POST;
    }

    enum Mode {
        PRE, POST
    }
}
