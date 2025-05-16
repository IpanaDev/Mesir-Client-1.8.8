package ipana.modules.player;

import ipana.events.*;
import ipana.managements.module.Modules;
import net.minecraft.item.*;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.NumberValue;
import ipana.utils.player.PlayerUtils;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.Event;

import pisi.unitedmeows.eventapi.event.listener.Listener;

public class QuickUse extends Module {
    public QuickUse() {
        super("QuickUse", Keyboard.KEY_COMMA, Category.Player, "Eats,drinks faster.");
    }

    public BoolValue goBack = new BoolValue("SwapBack", this, true, "Swap back to main slot.");
    public BoolValue resetOnGain = new BoolValue("GainReset", this, false, "Reset the AutoDrink after health gain");
    public BoolValue use = new BoolValue("Use", this, true, "Fast eat,drink.");
    public BoolValue place = new BoolValue("Place", this, true, "Fast place blocks.");
    public NumberValue<Double> duration = new NumberValue<>("Duration", this, 0.7, 0.05, 1D, 0.01, "Use Duration (old)16*50 = (new)0.8*1000.");
    private ItemStack newStack;
    private long usedMs;
    private boolean reset;
    private long stopMS;

    private Listener<EventPreUpdate> onPre = new Listener<EventPreUpdate>(event -> {
        if (place.getValue()) {
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                mc.rightClickDelayTimer = 0;
            } else {
                mc.rightClickDelayTimer = 0;
            }
        }
        if (reset && System.currentTimeMillis()-stopMS >= 75) {
            //PlayerUtils.debug("Waited for health gain resetting manually ("+(System.currentTimeMillis()-stopMS)+")");
            decide(Modules.AUTO_DRINK);
        }
    }).weight(Event.Weight.LOW);


    private Listener<EventFrame> onFrame = new Listener<EventFrame>(event -> {
        mc.timer.timerSpeed = 1f;
        AutoDrink autoDrink = Modules.AUTO_DRINK;
        long time = (long) (1000*duration.getValue());
        if (leftTime() > time) {
            int packetTime = (int) (time / 50);
            int packetSend = autoDrink.cancelMovement ? 33 : 33 - packetTime;
            for (int i = 0; i < packetSend; i++) {
                PlayerUtils.skipTicks();
            }

            mc.playerController.onStoppedUsingItem(mc.thePlayer);
            updateUseTime();
            autoDrink.moveStrategy.runTicks = 0;
            if (!resetOnGain.getValue()) {
                decide(autoDrink);
            } else {
                reset = true;
                stopMS = System.currentTimeMillis();
            }
            Modules.INV_HELPER.checkForSwap();
        }
    }).filter(e -> use.getValue() && canUse()).weight(Event.Weight.LOW);

    private Listener<EventPacketReceive> onReceive = new Listener<>(event -> {
       if (event.getState() == EventPacketReceive.PacketState.PRE) {
           if (event.getPacket() instanceof S09PacketHeldItemChange change) {

           } else if (event.getPacket() instanceof S06PacketUpdateHealth health) {//TODO: UNREAL BİLGİ FİX SOON
               if (health.getHealth() > mc.thePlayer.getHealth() && resetOnGain.getValue()) {
                   //PlayerUtils.debug("health gain "+(System.currentTimeMillis()-stopMS));
                   if (reset && System.currentTimeMillis()-stopMS >= 0) {
                       decide(Modules.AUTO_DRINK);
                   }
               }
           }
           if (mc.thePlayer != null && mc.thePlayer.getItemInUseDuration() > 0) {
               if (event.getPacket() instanceof S2FPacketSetSlot slot) {
                    if (slot.slot()-36 == mc.thePlayer.inventory.currentItem && slot.itemStack() != null && slot.itemStack().getItem() instanceof ItemPotion) {
                        newStack = slot.itemStack();
                    }
               } else if (event.getPacket() instanceof S19PacketEntityStatus item) {
                   if (item.getOpCode() == 9) {
                       event.setCancelled(true);
                   }
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
        if (heldItem != null && heldItem.getItem() instanceof ItemPotion potion) {
            if (potion.isEffectInstant(heldItem.getMetadata()) && heldItem.stackSize > 1) {
                for (PotionEffect effect : potion.getEffects(heldItem)) {
                    if (effect.getPotionID() == Potion.heal.id) {
                        float healAmount = (float)Math.max(4 << effect.getAmplifier(), 0);
                        boolean oldCheck = mc.thePlayer.getHealth()+healAmount <= autoDrink.health.getValue()+1;
                        if (oldCheck) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void decide(AutoDrink autoDrink) {
        if (goBack.getValue()) {
            if (validHealthToReset(autoDrink)) {
                autoDrink.tooMuchDamage = false;
                resetAutoDrink(autoDrink);
            } else {
                autoDrink.drinkAgain();
            }
        }
        reset = false;
    }

    private void resetAutoDrink(AutoDrink autoDrink) {
        if (autoDrink.isEnabled() && autoDrink.swap) {
            autoDrink.resetDrinking();
        } else {
            mc.thePlayer.inventory.currentItem = 0;
            mc.playerController.syncCurrentPlayItem();
        }
    }

    public void updateUseTime() {
        usedMs = System.currentTimeMillis();
    }

    public long leftTime() {
        return System.currentTimeMillis()-usedMs;
    }

    public boolean canUse() {
        return mc.thePlayer != null && mc.thePlayer.getItemInUse() != null && !(mc.thePlayer.getItemInUse().getItem() instanceof ItemBow) && !(mc.thePlayer.getItemInUse().getItem() instanceof ItemSword);
    }
}