package ipana.modules.player;

import ipana.events.*;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.ModeValue;
import ipana.managements.value.values.NumberValue;
import ipana.modules.player.modes.autopot.AutoPotMode;
import ipana.modules.player.modes.autopot.GroundPot;
import ipana.modules.player.modes.autopot.JumpPot;
import ipana.utils.player.PlayerUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.Event;

import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.util.ArrayDeque;

public class AutoPot extends Module {
    public AutoPot() {
        super("AutoHeal", Keyboard.KEY_NONE,Category.Player,"Automatically throws,drinks soup and potions.");
    }
    public ModeValue<AutoPotMode> mode = new ModeValue<>("Mode", this, "Throw mode.", JumpPot.class, GroundPot.class);
    public BoolValue second = new BoolValue("Second",this,false,"Throw another pot.");
    public NumberValue<Integer> heal = new NumberValue<>("Health",this,20,1,20,1,"Health check.");
    public boolean potting;
    private ArrayDeque<FuturePacket> futurePackets = new ArrayDeque<>();
    private int expectNullSlot;

    @Override
    public void onEnable() {
        mode.getValue().onEnable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        potting = false;
        super.onDisable();
    }

    private Listener<EventMoving> onMove = new Listener<EventMoving>(event -> mode.getValue().onMove(event)).weight(Event.Weight.MONITOR);
    private Listener<EventPreUpdate> onPre = new Listener<EventPreUpdate>(event -> mode.getValue().onPre(event)).weight(Event.Weight.LOWEST);
    private Listener<EventPostUpdate> onPost = new Listener<>(event -> mode.getValue().onPost(event));

    private Listener<EventPacketReceive> onReceive = new Listener<>(event -> {
        if (event.getState() != EventPacketReceive.PacketState.PRE) {
            return;
        }
        //PlayerUtils.debug(event.getPacket());
        if (event.getPacket() instanceof S30PacketWindowItems packet && !futurePackets.isEmpty()) {
            var futurePacket = futurePackets.poll();
            ItemStack itemStack1 = packet.getItemStacks()[futurePacket.pair1.slot];
            ItemStack itemStack2 = packet.getItemStacks()[futurePacket.pair2.slot];
            boolean potionMatch = itemStack1 == null && futurePacket.pair1.stack.getItem() instanceof ItemPotion /*&& futurePacket.pair1.stack.stackSize == 1*/ || itemStack1 != null && itemStack1.getItem() == futurePacket.pair1.stack.getItem();
            boolean handMatch = itemStack2.getItem() == futurePacket.pair2.stack.getItem();
            //PlayerUtils.debug(potionMatch+", "+handMatch+" : "+itemStack1+", "+itemStack2);
            if (potionMatch && handMatch) {
                packet.getItemStacks()[futurePacket.pair1.slot] = itemStack2;
                packet.getItemStacks()[futurePacket.pair2.slot] = itemStack1;
                //PlayerUtils.debug("Potion slots corrected.");
            }
        } else if (event.getPacket() instanceof S2FPacketSetSlot packet) {
            if (expectNullSlot != -1 && packet.slot() == expectNullSlot && packet.itemStack() == null) {
                event.setCancelled(true);
                expectNullSlot = -1;
            }
        }
    });

    public void throwPotion() {
        int potionSlot = isCorrectPotion(-1);
        if (potionSlot == -1) {
            return;
        }

        expectNullSlot = mc.thePlayer.inventory.currentItem + 36;
        ItemStack potStack = mc.thePlayer.inventoryContainer.getSlot(potionSlot).getStack();
        PlayerUtils.packet(new C0EPacketClickWindow(mc.thePlayer.inventoryContainer.windowId, potionSlot, mc.thePlayer.inventory.currentItem, 2, null, mc.thePlayer.openContainer.getNextTransactionID(mc.thePlayer.inventory)));
        futurePackets.add(new FuturePacket(
                new SlotStack(mc.thePlayer.inventory.currentItem + 36, potStack),
                new SlotStack(potionSlot, mc.thePlayer.getHeldItem())
        ));
        if (--potStack.stackSize <= 0) {
            mc.thePlayer.inventoryContainer.getSlot(potionSlot).putStack(null);
        }
        PlayerUtils.packet(new C08PacketPlayerBlockPlacement(null));
        PlayerUtils.packet(new C0EPacketClickWindow(mc.thePlayer.inventoryContainer.windowId, potionSlot, mc.thePlayer.inventory.currentItem, 2, null, mc.thePlayer.openContainer.getNextTransactionID(mc.thePlayer.inventory)));
    }

    public int isCorrectPotion(int invalid) {
        for (int i = 0; i < 45; i++) {
            if (i == invalid) {
                continue;
            }
            var slot = mc.thePlayer.inventoryContainer.getSlot(i);
            if (slot.getHasStack()) {
                ItemStack is = slot.getStack();
                Item item = is.getItem();
                if (item instanceof ItemPotion potion) {
                    var effects = potion.getEffects(is);
                    if (effects == null) {
                        continue;
                    }
                    for (PotionEffect o : effects) {
                        if (o.getPotionID() == Potion.heal.id && ItemPotion.isSplash(is.getItemDamage())) {
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    public enum Mode {
        Ground, Jump
    }

    private record FuturePacket(SlotStack pair1, SlotStack pair2) {

    }

    private record SlotStack(int slot, ItemStack stack) {

    }
}
