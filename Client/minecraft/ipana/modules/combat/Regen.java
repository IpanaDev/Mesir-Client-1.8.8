package ipana.modules.combat;

import ipana.events.EventPacketReceive;
import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.NumberValue;
import ipana.utils.player.PlayerUtils;
import net.minecraft.entity.DataWatcher;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;

public class Regen extends Module {

    public Regen() {
        super("Regen", Keyboard.KEY_NONE, Category.Combat, "Regenerate faster.");
        delay.setCondition(() -> autoPacket.getValue());
        packets.setCondition(() -> !autoPacket.getValue());
    }
    private NumberValue<Integer> health = new NumberValue<>("Health", this, 15, 1, 20, 1, "Health to regen");
    private BoolValue autoPacket = new BoolValue("Auto Packet", this, false, "");
    private NumberValue<Integer> delay = new NumberValue<>("Delay",this,1, 1,10,1,"Delay to send packets");
    private NumberValue<Integer> packets = new NumberValue<>("Packets",this,5, 5,100,5,"Potion packets");
    private int ticks;


    private Listener<EventPreUpdate> onPost = new Listener<EventPreUpdate>(event -> {

        if (autoPacket.getValue()) {
            if (ticks > 1) {
                regen(mc.thePlayer.getHealth());
                ticks--;
            }
        } else if (mc.thePlayer.getHealth() <= health.getValue()) {
            for (int i = 0; i < packets.getValue(); i++) {
                PlayerUtils.skipTicks(false);
            }
        }
    }).filter(f -> checks()).weight(Event.Weight.MONITOR);

    private Listener<EventPacketReceive> onReceive = new Listener<EventPacketReceive>(event -> {
        if (event.getPacket() instanceof S1CPacketEntityMetadata packet) {
            if (mc.thePlayer.getEntityId() == packet.getEntityId()) {
                for (DataWatcher.WatchableObject watchableObject : packet.watchableObjects()) {
                    if (watchableObject.getDataValueId() == 6) {
                        float health = (float) watchableObject.getObject();
                        if (health < mc.thePlayer.getHealth()) {
                            regen(health);
                            ticks = delay.getValue();
                        }
                    }
                }
            }
        } else if (event.getPacket() instanceof S06PacketUpdateHealth packet) {
            event.setCancelled(true);
        }
    }).filter(f -> f.getState() == EventPacketReceive.PacketState.PRE && autoPacket.getValue() && checks()).weight(Event.Weight.MONITOR);


    private void regen(float health) {
        PotionEffect effect = mc.thePlayer.getActivePotionEffect(Potion.regeneration);

        int tickCalc = 50 >> effect.getAmplifier();
        float diff = this.health.getValue() - health;
        float calc = diff * tickCalc / delay.getValue();
        int i = 0;

        for (; i < calc; i++) {
            PlayerUtils.skipTicks(false);
        }
    }

    private boolean checks() {
        return mc.thePlayer != null && mc.theWorld != null && mc.thePlayer.isPotionActive(Potion.regeneration);
    }
}
