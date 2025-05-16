package ipana.modules.combat.modes.criticals;


import ipana.events.EventPacketSend;
import ipana.managements.module.Module;
import ipana.managements.module.ModuleManager;
import ipana.managements.module.Modules;
import ipana.modules.combat.Criticals;
import ipana.modules.combat.KillAura;
import net.minecraft.network.play.client.C03PacketPlayer;

public class Packets extends CritMode {
    public Packets(Criticals parent) {
        super("Packets",parent);
    }

    @Override
    public void onSend(EventPacketSend event) {
        Criticals criticals = getParent();
        double critY = criticals.critY.getValue();
        if (event.getPacket() instanceof C03PacketPlayer) {
            if (event.getState() == EventPacketSend.PacketState.PRE) {
                C03PacketPlayer player = (C03PacketPlayer) event.getPacket();
                boolean gayCheck = (!Modules.KILL_AURA.isEnabled() && mc.thePlayer.swingProgressInt > 0 && mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null);
                boolean critOffset = mc.thePlayer.isCollidedVertically && mc.thePlayer.swingProgressInt == 1;
                if (gayCheck) {
                    if (critOffset) {
                        player.y+=critY;
                    }
                    player.onGround=false;
                }
            }
        }
        super.onSend(event);
    }
}
