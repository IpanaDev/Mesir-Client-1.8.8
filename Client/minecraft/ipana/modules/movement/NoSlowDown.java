package ipana.modules.movement;

import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.BoolValue;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;
import ipana.utils.player.PlayerUtils;
import pisi.unitedmeows.eventapi.event.Event;

import pisi.unitedmeows.eventapi.event.listener.Listener;

public class NoSlowDown extends Module {
    public NoSlowDown() {
        super("NoSlowDown", Keyboard.KEY_NONE,Category.Movement,"No slows when eats,drinks.");
    }
    private BoolValue noPacket = new BoolValue("NoPacket",this,false,"Send no packets.");

    private Listener<EventPreUpdate> onPre = new Listener<EventPreUpdate>(event -> {
        PlayerUtils.packet(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
    }).filter(filter -> mc.thePlayer.isBlocking() && PlayerUtils.isMoving2() && !noPacket.getValue()).weight(Event.Weight.LOWEST);

    private Listener<EventPostUpdate> onPost = new Listener<EventPostUpdate>(event -> {
        PlayerUtils.packet(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
    }).filter(filter -> mc.thePlayer.isBlocking() && PlayerUtils.isMoving2() && !noPacket.getValue()).weight(Event.Weight.LOWEST);
}
