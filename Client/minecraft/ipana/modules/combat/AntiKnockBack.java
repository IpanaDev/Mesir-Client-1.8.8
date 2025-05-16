package ipana.modules.combat;

import ipana.events.EventPacketReceive;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.managements.value.values.EnumValue;
import ipana.managements.value.values.NumberValue;
import ipana.modules.movement.Fly;
import ipana.modules.movement.modes.fly.BowFly;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.listener.Listener;

public class AntiKnockBack extends Module {

    public AntiKnockBack() {
        super("AntiKB", Keyboard.KEY_M,Category.Combat,"Anti-Knock back.");
    }

    private NumberValue<Integer> multiplier = new NumberValue<>("Multiplier",this,0,0,100,5,"Knock back multiplier.");
    private EnumValue<Mode> mode = new EnumValue<>("Mode", this, Mode.class, "KB modes.");

    private Listener<EventPreUpdate> onPre = new Listener<EventPreUpdate>(event -> {
        if (mc.thePlayer.hurtTime > 0) {
            mc.thePlayer.motionX *= multiplier.getValue() / 100f;
            mc.thePlayer.motionZ *= multiplier.getValue() / 100f;
        }
    }).filter(f -> mode.getValue() == Mode.Motion);

    private Listener<EventPacketReceive> onReceive = new Listener<EventPacketReceive>(event -> {
        S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.getPacket();
        Fly fly = Modules.FLY;
        if (!fly.isEnabled() || !(fly.mode.getValue() instanceof BowFly)) {
            if (mc.theWorld != null && mc.theWorld.getEntityByID(packet.getEntityID()) == mc.thePlayer) {
                if (multiplier.getValue() <= 0) {
                    event.setCancelled(true);
                } else {
                    packet.motionX *= (int) (multiplier.getValue() / 100f);
                    packet.motionY *= (int) (multiplier.getValue() / 100f);
                    packet.motionZ *= (int) (multiplier.getValue() / 100f);
                }
            }
        }
    }).filter(this::check);

    private boolean check(EventPacketReceive event) {
        setSuffix("%"+" "+multiplier.getValue());
        return event.getPacket() instanceof S12PacketEntityVelocity && mode.getValue() == Mode.Packet;
    }

    @Override
    public void onSuffixChange() {
        setSuffix("%"+" "+multiplier.getValue());
        super.onSuffixChange();
    }

    enum Mode {
        Packet, Motion
    }
}
