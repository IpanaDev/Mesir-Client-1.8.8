package ipana.modules.combat;

import ipana.events.EventPacketSend;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.ModeValue;
import ipana.managements.value.values.NumberValue;
import ipana.modules.combat.modes.criticals.CritMode;
import ipana.modules.combat.modes.criticals.MotionY;
import ipana.modules.combat.modes.criticals.Packets;
import ipana.modules.combat.modes.killaura.*;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.listener.Listener;


public class Criticals extends Module {
    public NumberValue<Double> critY = new NumberValue<>("Height",this,0.075,0.01,0.42,0.005,"Crit pos y.");
    public BoolValue moreDura = new BoolValue("MoreDura",this,false,"Does more durability.", () -> {
        KillAura ka = Modules.KILL_AURA;
        KaMode m = ka.mode.getValue();
        return m instanceof Single || m instanceof TickMode || m instanceof HurtTimeMode || m instanceof RTE2025;
    });
    public BoolValue lessDura = new BoolValue("LessDura",this,false,"Does less durability (Tick).", () -> Modules.KILL_AURA.mode.getValue() instanceof TickMode);
    public ModeValue<CritMode> mode = new ModeValue<>("Mode", this, "Criticals modes.", Packets.class, MotionY.class);

    public Criticals() {
        super("Criticals", Keyboard.KEY_P,Category.Combat,"Hits critical.");
    }

    private Listener<EventPreUpdate> onPre = new Listener<>(event -> {
        setSuffix(mode.getValue().getName());
        mode.getValue().onPre(event);
    });

    private Listener<EventPacketSend> onSend = new Listener<>(event -> {
        mode.getValue().onSend(event);
    });

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onSuffixChange() {
        setSuffix(mode.getValue().getName());
        super.onSuffixChange();
    }
}
