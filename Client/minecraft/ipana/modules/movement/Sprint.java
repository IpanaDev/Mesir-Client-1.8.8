package ipana.modules.movement;

import ipana.events.EventPacketSend;
import ipana.events.EventTick;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.managements.value.values.BoolValue;
import ipana.modules.combat.KillAura;
import ipana.modules.combat.modes.killaura.Legit;
import ipana.utils.player.PlayerUtils;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import org.lwjgl.input.Keyboard;

import org.lwjgl.opengl.Display;
import pisi.unitedmeows.eventapi.event.listener.Listener;

public class Sprint extends Module {
    public BoolValue multi = new BoolValue("MultiDir",this,false,"MultiDirection sprint.");
    public BoolValue client = new BoolValue("Client",this,false,"Client side.");
    public BoolValue blatant = new BoolValue("Blatant",this,false,"Blatant sprint.");
    public Sprint() {
        super("Sprint", Keyboard.KEY_NONE,Category.Movement,"Automatically sprints.");
    }

    public Listener<EventTick> onTick = new Listener<EventTick>(event -> mc.thePlayer.setSprinting(canSprint())).filter(filter -> mc.thePlayer != null && blatant.getValue());

    public Listener<EventPacketSend> onSend = new Listener<EventPacketSend>(event -> {
        C0BPacketEntityAction.Action action = ((C0BPacketEntityAction)event.getPacket()).getAction();

        if (action == C0BPacketEntityAction.Action.START_SPRINTING) {
            event.setCancelled(true);
        }
    }).filter(filterEvent -> filterEvent.getPacket() instanceof C0BPacketEntityAction && client.getValue());


    public boolean canSprint() {
        return !mc.thePlayer.isCollidedHorizontally && (multi.getValue() ? (PlayerUtils.isMoving2()) : mc.thePlayer.moveForward > 0);
    }
}
