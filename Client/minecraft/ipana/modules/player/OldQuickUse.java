package ipana.modules.player;

import ipana.events.*;
import ipana.managements.module.Modules;
import ipana.modules.combat.KillAura;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.NumberValue;
import ipana.utils.player.PlayerUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.Event;

import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.util.Comparator;

public class OldQuickUse extends Module {
    public OldQuickUse() {
        super("QuickUse", Keyboard.KEY_COMMA, Category.Player, "Eats,drinks faster.");
    }

    public BoolValue goBack = new BoolValue("SwapBack", this, true, "Swap back to main slot.");
    public BoolValue use = new BoolValue("Use", this, true, "Fast eat,drink.");
    public BoolValue place = new BoolValue("Place", this, true, "Fast place blocks.");
    public BoolValue pre = new BoolValue("Pre", this, false, "Drink on pre update (experimental).");
    public NumberValue<Integer> time = new NumberValue<>("Time", this, 16, 1, 20, 1, "Use time.");
    private ItemStack newStack;

    private Listener<EventPostUpdate> onPost = new Listener<EventPostUpdate>(event -> drink()).filter(e -> !pre.getValue()).weight(Event.Weight.LOW);

    private Listener<EventPreUpdate> onPre = new Listener<EventPreUpdate>(event -> drink()).filter(e -> pre.getValue()).weight(Event.Weight.LOW);

    private void drink() {
        if (use.getValue() && canUse()) {
            mc.timer.timerSpeed = 1f;
            AutoDrink autoDrink = Modules.AUTO_DRINK;
            if (mc.thePlayer.getItemInUseDuration() == time.getValue()) {
                for (int i = 0; i < 33 - time.getValue(); i++) {
                    PlayerUtils.skipTicks();
                }
                mc.playerController.onStoppedUsingItem(mc.thePlayer);
                Modules.INV_HELPER.checkForSwap();
                if (goBack.getValue()) {
                    if (validHealthToReset(autoDrink)) {
                        autoDrink.tooMuchDamage = false;
                        resetAutoDrink(autoDrink);
                    } else {
                        autoDrink.drinkAgain();
                    }
                }
            }
        }
        if (place.getValue()) {
            mc.playerController.blockHitDelay = 0;
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                mc.rightClickDelayTimer = 0;
            } else {
                mc.rightClickDelayTimer = 0;
            }
        }
    }

    private Listener<EventPacketReceive> onReceive = new Listener<>(event -> {
        if (event.getState() == EventPacketReceive.PacketState.PRE) {
            if (mc.thePlayer != null && mc.thePlayer.getItemInUseDuration() > 0) {
                if (event.getPacket() instanceof S2FPacketSetSlot slot) {
                    if (slot.slot()-36 == mc.thePlayer.inventory.currentItem && slot.itemStack() != null && slot.itemStack().getItem() instanceof ItemPotion) {
                        newStack = slot.itemStack();
                    }
                } else if (event.getPacket() instanceof S19PacketEntityStatus item && item.getOpCode() == 9) {
                    event.setCancelled(true);
                }
            }
        }
    });

    private Listener<EventClearItemUse> onClear = new Listener<>(event -> {
        if (newStack == mc.thePlayer.getHeldItem()) {
            event.setCancelled(true);
        }
    });


    private boolean validHealthToReset(AutoDrink autoDrink) {
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem.getItem() instanceof ItemPotion potion) {
            if (potion.isEffectInstant(heldItem.getMetadata()) && heldItem.stackSize > 1) {
                for (PotionEffect effect : potion.getEffects(heldItem)) {
                    if (effect.getPotionID() == Potion.heal.id) {
                        float healAmount = (float)Math.max(4 << effect.getAmplifier(), 0);
                        boolean oldCheck = mc.thePlayer.getHealth()+healAmount <= autoDrink.health.getValue()+1;
                        boolean newCheck = mc.thePlayer.getHealth() < 6;
                        if (oldCheck) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void resetAutoDrink(AutoDrink autoDrink) {
        if (autoDrink.isEnabled() && autoDrink.swap) {
            autoDrink.resetDrinking();
        } else {
            mc.thePlayer.inventory.currentItem = 0;
            mc.playerController.syncCurrentPlayItem();
        }
    }

    public boolean canUse() {
        return mc.thePlayer.getItemInUse() != null && !(mc.thePlayer.getItemInUse().getItem() instanceof ItemBow) && !(mc.thePlayer.getItemInUse().getItem() instanceof ItemSword);
    }
}