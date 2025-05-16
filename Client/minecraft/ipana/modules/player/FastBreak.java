package ipana.modules.player;

import baritone.api.BaritoneAPI;
import baritone.api.event.events.BlockInteractEvent;
import ipana.events.EventFrame;
import ipana.events.EventPacketSend;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.managements.value.values.BoolValue;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.listener.Listener;

public class FastBreak extends Module {
    public BoolValue fastInteract = new BoolValue("Interact", this, true, "bok.");
    public BoolValue breakDamage = new BoolValue("Damage", this, true, "bok.");
    private BlockPos breakingPos;
    private long breakMs;

    public FastBreak() {
        super("FastBreak", Keyboard.KEY_NONE, Category.Player, "Fast break.");
    }

    private Listener<EventFrame> onFrame = new Listener<>(event -> {
        if (breakDamage.getValue() && mc.thePlayer != null && mc.theWorld != null && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            BlockPos blockpos = mc.objectMouseOver.getBlockPos();
            Block block = mc.theWorld.getBlockState(blockpos).getBlock();
            long ncpFastBreakDelay = 50;
            //long ncpFastBreakDelay = 100;
            //TODO: ROUNDING UP 1f/block.getPlayerRelativeBlockHardness(mc.thePlayer)
            long duration = (long) (1f/block.getPlayerRelativeBlockHardness(mc.thePlayer)*50) - ncpFastBreakDelay;
            if (duration % 50 != 0) {
                duration += duration % 50;
            }
            if (breakingPos != null && breakingPos.equals(blockpos) && System.currentTimeMillis() - breakMs >= duration) {
                //PlayerUtils.debug(System.currentTimeMillis() - breakMs, duration, 1f/block.getPlayerRelativeBlockHardness(mc.thePlayer)*50);
                breakMs = System.currentTimeMillis();
                PlayerUtils.packet(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockpos, mc.objectMouseOver.sideHit));
                mc.playerController.onPlayerDestroyBlock(blockpos, mc.objectMouseOver.sideHit);
                Nuker nuker = Modules.NUKER;
                if (nuker.isEnabled()) {
                    BlockPos pos = nuker.getBlockPos();
                    if (pos != null) {
                        nuker.setRotations(pos, null);
                    }
                }
                mc.entityRenderer.getMouseOver(1f);
                //new mouse click
                if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                    mc.leftClickCounter = 0;
                    mc.clickMouse();
                }
            }
        }
    });

    private Listener<EventPacketSend> onSend = new Listener<>(event -> {
        if (event.getState() == EventPacketSend.PacketState.POST) {
            if (event.getPacket() instanceof C07PacketPlayerDigging c07) {
                switch (c07.getStatus()) {
                    case START_DESTROY_BLOCK -> {
                        breakingPos = c07.getPosition();
                        breakMs = System.currentTimeMillis();
                    }
                    case ABORT_DESTROY_BLOCK, STOP_DESTROY_BLOCK -> breakingPos = null;
                }
            } else if (event.getPacket() instanceof C0EPacketClickWindow window) {
                //PlayerUtils.debug(window.getSlotId()+" : "+window.getMode()+" : "+window.getActionNumber());
            }
        }
    });
}
