package ipana.modules.movement;

import ipana.events.*;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.EnumValue;
import ipana.managements.value.values.ModeValue;
import ipana.managements.value.values.NumberValue;
import ipana.modules.movement.modes.fly.*;
import ipana.utils.player.PlayerUtils;
import org.lwjgl.input.Keyboard;

import pisi.unitedmeows.eventapi.event.listener.Listener;

public class Fly extends Module {
    public ModeValue<FlyMode> mode = new ModeValue<>("Mode",this,"Fly methods.", Vanilla.class, OldNCPGlide.class, OldNCPV2.class, BowFly.class, LongJump.class, OldLongJump.class, OldHighJump.class, Vehicle.class, Damage.class);
    public EnumValue<GlideMode> glideMode = new EnumValue<>("GlideMode",this, GlideMode.class,"Fly methods.", () -> mode.getValue() instanceof OldNCPGlide);
    public BoolValue latest = new BoolValue("Latest",this,false,"Latest option for some modes.");
    public BoolValue stopLimit = new BoolValue("StopAtLimit",this,false,"Stops at fly limit.", () -> latest.getValue() && mode.getValue() instanceof LongJump);
    public BoolValue instantFlag = new BoolValue("InstantFlag",this,true,"Instant flag prediction.", () -> latest.getValue() && mode.getValue() instanceof LongJump);
    public BoolValue autoDisable = new BoolValue("AutoDisable",this,false,"Auto disable when flew.");
    public NumberValue<Double> speed = new NumberValue<>("Speed",this,1.0,0.1,10.0,0.1,"Speed number.");

    public Fly() {
        super("Fly", Keyboard.KEY_GRAVE,Category.Movement,"Allow you to fly.");
    }

    private Listener<EventMoveInput> onMoveInput = new Listener<>(event -> {
        mode.getValue().onMove(event);
    });

    @Override
    public void onEnable() {
        mode.getValue().onEnable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        mode.getValue().onDisable();
        mc.thePlayer.speedInAir = 0.02f;
        mc.timer.timerSpeed = 1;
        super.onDisable();
    }

    private Listener<EventMoving> onMove = new Listener<>(event -> {
        mode.getValue().onMove(event);
    });
    private Listener<EventMouse> onMouse = new Listener<>(event -> {
        mode.getValue().onMouse(event);
    });
    private Listener<EventPreUpdate> onPre = new Listener<>(event -> {
        onSuffixChange();
        mode.getValue().onPre(event);
    });
    private Listener<EventPostUpdate> onPost = new Listener<>(event -> {
        mode.getValue().onPost(event);
    });
    private Listener<EventPacketReceive> onReceive = new Listener<>(event -> {
        mode.getValue().onReceive(event);
    });
    private Listener<EventSetBack> onSetBack = new Listener<>(event -> {
        mode.getValue().onSetBack(event);
    });
    @Override
    public void onSuffixChange() {
        String suffix =
                mode.getValue().getSuffix() != null ?
                mode.getValue().getName()+" - "+mode.getValue().getSuffix() :
                mode.getValue().getName();
        setSuffix(suffix);
        super.onSuffixChange();
    }
    private double hello(double startY) {
        double posY = 0;
        double motionY = startY;
        while (motionY > 0) {
            double friction = (motionY - 0.08) * 0.9800000190734863D;
            if (friction > 0) {
                posY += motionY;
            }
            motionY = friction;
        }
        return posY;
    }

    public enum GlideMode {
        Crit,NonCrit,Manual
    }
}
