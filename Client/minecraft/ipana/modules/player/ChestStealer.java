package ipana.modules.player;

import ipana.events.EventPreUpdate;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.managements.value.Value;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.NumberValue;
import ipana.modules.combat.KillAura;
import ipana.utils.Timer;
import ipana.utils.player.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import org.lwjgl.input.Keyboard;

import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

public class ChestStealer extends Module {
    public ChestStealer() {
        super("ChestStealer", Keyboard.KEY_K, Category.Player,"Steal chest.");
    }

    public NumberValue<Integer> delay = new NumberValue<>("Delay",this,6,1,20,1,"Steal delay.");
    public BoolValue autoClose = new BoolValue("AutoClose", this, false, "Auto close when chest stolen.");
    private Queue<ItemSlots> things = new ArrayDeque<>();
    private int ticks;

    @Override
    public void onEnable() {
        reset();
        super.onEnable();
    }

    private Listener<EventPreUpdate> onPre = new Listener<EventPreUpdate>(event -> {
        KillAura ka = Modules.KILL_AURA;
        if (!ka.targets.isEmpty()) {
            reset();
            return;
        }
        if (mc.currentScreen instanceof GuiChest chest) {

            boolean siciyom = false;
            if (things.isEmpty()) {
                for (int i = 0; i < chest.inventoryRows * 9; i++) {
                    Slot slot = chest.inventorySlots.inventorySlots.get(i);
                    if (slot.getStack() != null && filter(slot.getStack())) {
                        things.add(new ItemSlots(i));
                    }
                }
                siciyom = things.isEmpty();
            }
            if (siciyom) {
                if (autoClose.getValue()) {
                    mc.thePlayer.closeScreen();
                    mc.displayGuiScreen(null);
                    PlayerUtils.fixInventory();
                }
            }
            if (!things.isEmpty() && ticks <= 0) {
                boolean lastItem = things.size() == 1;
                ItemSlots slot = things.poll();
                this.mc.playerController.windowClick(chest.inventorySlots.windowId, slot.slotId, 0, 1, mc.thePlayer);

                //chest.handleMouseClick(slot.slot, slot.slot.slotNumber, 0, 1);
                ticks = delay.getValue();
                if (lastItem && autoClose.getValue()) {
                    mc.thePlayer.closeScreen();
                    mc.displayGuiScreen(null);
                }
            }
            ticks--;
        } else {
            reset();
        }
    });

    private boolean filter(ItemStack stack) {
        Item item = stack.getItem();
        boolean swordCheck = item instanceof ItemSword sword && isBetterSword(stack, sword);
        boolean toolCheck = (item instanceof ItemAxe || item instanceof ItemSpade || item instanceof ItemPickaxe) && isBetterStrVsBlock(stack, (ItemTool) item);
        return swordCheck || toolCheck || item instanceof ItemBow || item == Items.arrow || item instanceof ItemBlock || item instanceof ItemFood || item instanceof ItemArmor || item == Item.getItemFromBlock(Blocks.tnt);
    }

    private boolean isBetterSword(ItemStack stack, ItemSword sword) {
        for (int i = 0; i < 36; i++) {
            ItemStack stacks = mc.thePlayer.inventory.mainInventory[i];
            if (stacks != null && stacks.getItem() instanceof ItemSword invSword) {
                int invLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId,stacks);
                int sLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId,stack);
                float invDamage = invSword.getDamageVsEntity() + invLevel*1.25f;
                float sDamage = sword.getDamageVsEntity() + sLevel*1.25f;
                if (invDamage >= sDamage) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isBetterStrVsBlock(ItemStack stack, ItemTool tool) {
        for (int i = 0; i < 36; i++) {
            ItemStack stacks = mc.thePlayer.inventory.mainInventory[i];
            if (stacks != null && stacks.getItem() instanceof ItemTool invTool && tool.getClass() == invTool.getClass()) {
                int invLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId,stacks);
                int sLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId,stack);
                Block block = tool instanceof ItemAxe ? Blocks.log : tool instanceof ItemPickaxe ? Blocks.obsidian : tool instanceof ItemSpade ? Blocks.dirt : Blocks.stone;
                float invDamage = invTool.getStrVsBlock(stacks, block) + invLevel*invLevel+1;
                float sDamage = invTool.getStrVsBlock(stack, block) + sLevel*sLevel+1;
                if (invDamage >= sDamage) {
                    return false;
                }
            }
        }
        return true;
    }

    private void reset() {
        things.clear();
        ticks = delay.getValue();
    }

    class ItemSlots {
        int slotId;

        public ItemSlots(int slotId) {
            this.slotId = slotId;
        }
    }

}
