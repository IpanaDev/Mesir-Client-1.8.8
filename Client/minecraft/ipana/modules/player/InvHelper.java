package ipana.modules.player;

import ipana.events.EventPacketReceive;
import ipana.events.EventPacketSend;
import ipana.events.EventPreUpdate;
import ipana.events.EventPostUpdate;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.managements.value.Value;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.NumberValue;
import ipana.utils.Timer;
import ipana.utils.player.PlayerUtils;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.Event;

import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author superblaubeere27
 */

public class InvHelper extends Module {
    BoolValue autoArmor = new BoolValue("AutoArmor",this,true,"Swap To Best Armor");
    BoolValue changeArmor = new BoolValue("Change",this,false,"Change armor before it breaks", () -> autoArmor.getValue());
    BoolValue autoSword = new BoolValue("AutoSword",this,false,"Swap Sword To Current Slot");
    BoolValue dropTrash = new BoolValue("DropTrash",this,true,"Drop Trashes");
    NumberValue<Integer> delay = new NumberValue<>("Delay",this,300,0,1000,50,"Helping Delay");
    private Timer dropTimer = new Timer();
    private Timer  swordTimer = new Timer();
    private Timer armorTimer = new Timer();
    private int[] bestArmorDamageReducememt;
    private int[] bestArmorSlots;
    private float bestSwordDamage;
    private int bestSwordSlot;
    private List<Integer> trash = new ArrayList<>();
    private List<Consumer<Boolean>> trolls = new ArrayList<>();
    private boolean sictimBePatient;

    public InvHelper() {
        super("InvHelper",Keyboard.KEY_LBRACKET, Category.Player,"Automatically Drops,Wears Swords, Armors");
    }

    private Listener<EventPreUpdate> onPre = new Listener<EventPreUpdate>(event -> {

        if (mc.currentScreen instanceof GuiChest || mc.currentScreen instanceof GuiContainerCreative) {
            armorTimer.reset();
            return;
        }
        AutoDrink autoDrink = Modules.AUTO_DRINK;
        if (autoArmor.getValue() && changeArmor.getValue() && (!autoDrink.isEnabled() || !autoDrink.swap)) {
            ItemStack[] armorsInv = getBestArmors();
            for (int i = 0; i < mc.thePlayer.inventory.armorInventory.length; i++) {
                ItemStack armor = mc.thePlayer.inventory.armorInventory[3 - i];
                if (armor == null || armorsInv[i] == null) {
                    continue;
                }
                int durability = armor.getMaxDamage() - armor.getItemDamage();
                if (durability <= 20 && armor.stackSize == 1) {
                    PlayerUtils.debug("Dropping armor (Dura: "+durability+")");
                    mc.playerController.windowClick(0, 5+i, 0, 4, mc.thePlayer);
                }
            }
        }
        searchForItems();
        for (int i = 0; i < 4; i++) {
            if (bestArmorSlots[i] != -1 && autoArmor.getValue()) {
                int bestSlot = bestArmorSlots[i];

                ItemStack oldArmor = mc.thePlayer.inventory.armorItemInSlot(i);

                if (armorTimer.delay(delay.getValue())) {
                    if (oldArmor != null && oldArmor.getItem() instanceof ItemSkull) {
                        return;
                    }
                    int finalI = i;
                    trolls.add(c -> {
                        if (oldArmor != null && oldArmor.getItem() != null) {
                            mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, 8 - finalI, 0, 1, mc.thePlayer);
                        }
                        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, bestSlot < 9 ? bestSlot + 36 : bestSlot, 0, 1, mc.thePlayer);
                    });
                    QuickUse quickUse = Modules.QUICK_USE;
                    if (!quickUse.canUse()) {
                        consumeSwaps();
                        sictimBePatient = false;
                    } else {
                        int predict = 2;
                        long time = (long) (1000*quickUse.duration.getValue());
                        int ticksLeft = (int) ((time-quickUse.leftTime()) / 50);
                        sictimBePatient = mc.thePlayer.hurtResistantTime - ticksLeft >= 10 + predict;
                    }
                    if (!sictimBePatient && quickUse.canUse()) {
                        consumeSwaps();
                        if (mc.thePlayer.getHealth() <= 6.5) {
                            PlayerUtils.debug("her türlü sıçtın kanka uğraşma");
                        }
                        mc.playerController.onStoppedUsingItem(mc.thePlayer);
                        mc.playerController.sendUseItem(mc.thePlayer,mc.theWorld,mc.thePlayer.getHeldItem());
                        long time = (long) (1000*quickUse.duration.getValue());
                        long left = time-quickUse.leftTime();
                        PlayerUtils.debug("Saved: "+left);
                        quickUse.updateUseTime();
                    }
                    armorTimer.reset();
                }
            }
        }

        if (bestSwordSlot != -1 && bestSwordDamage != -1 && autoSword.getValue() && !mc.thePlayer.isUsingItem()) {
            if (swordTimer.delay(delay.getValue())) {
                mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, bestSwordSlot < 9 ? bestSwordSlot + 36 : bestSwordSlot, 0, 2, mc.thePlayer);
                swordTimer.reset();
            }
        }
        if (dropTrash.getValue()) {
            searchForTrash();
            for (Integer integer : trash) {
                if (dropTimer.delay(delay.getValue())) {
                    mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, integer < 9 ? integer + 36 : integer, 0, 4, mc.thePlayer);
                    dropTimer.reset();
                }
            }
        }
    }).weight(Event.Weight.LOWEST);

    private Listener<EventPacketSend> onSend = new Listener<EventPacketSend>(event -> {

    }).filter(event -> event.getState() == EventPacketSend.PacketState.PRE);

    private Listener<EventPacketReceive> onReceive = new Listener<EventPacketReceive>(event -> {
        if (event.getPacket() instanceof S2FPacketSetSlot slot) {
            /*
            if (slot.itemStack() == null && autoArmor.getValue()) {
                int slotId = slot.slot()-5;
                if (slotId >= 0 && slotId < 4) {
                    mc.thePlayer.inventory.armorInventory[slotId] = null;
                    int bestSlot = getArmorForSlot(slotId);
                    if (bestSlot != -1) {
                        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, bestSlot < 9 ? bestSlot + 36 : bestSlot-3, 0, 1, mc.thePlayer);
                        if (mc.thePlayer.isEating()) {
                            mc.playerController.onStoppedUsingItem(mc.thePlayer);
                            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                        }
                        mc.thePlayer.inventory.armorInventory[slotId] = mc.thePlayer.inventory.getStackInSlot(bestSlot);
                    }
                }
            }

             */
        }
    }).filter(f -> f.getState().equals(EventPacketReceive.PacketState.PRE));

    private void consumeSwaps() {
        for (Consumer<Boolean> ataturk : trolls) {
            ataturk.accept(true);
        }
        trolls.clear();
    }

    public void checkForSwap() {
        if (sictimBePatient) {
            consumeSwaps();
            sictimBePatient = false;
        }
    }
    private ItemStack[] getBestArmors() {
        ItemStack[] armors = new ItemStack[4];
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
            if (itemStack != null && itemStack.getItem() instanceof ItemArmor armor && (armors[armor.armorType] == null || armors[armor.armorType].stackSize < itemStack.stackSize)) {
                armors[armor.armorType] = itemStack;
            }
        }
        return armors;
    }

    private void searchForTrash() {
        trash.clear();
        bestArmorDamageReducememt = new int[4];
        bestArmorSlots = new int[4];

        Arrays.fill(bestArmorDamageReducememt,-1);
        Arrays.fill(bestArmorSlots,-1);
        bestSwordDamage = -1;
        bestSwordSlot = -1;

        List<Integer>[] allItems = new List[4];
        List<Integer> allSwords = new ArrayList<>();

        for(int i = 0; i < bestArmorSlots.length; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.armorItemInSlot(i);

            allItems[i] = new ArrayList<>();

            if (itemStack != null && itemStack.getItem() != null) {
                if (itemStack.getItem() instanceof ItemArmor armor) {
                    bestArmorDamageReducememt[i] = armor.damageReduceAmount;
                    bestArmorSlots[i] = 8+i;
                }
            }
        }

        for (int i = 0; i < 9*4; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);

            if (itemStack == null || itemStack.getItem() == null) continue;

            if (itemStack.getItem() instanceof ItemArmor armor) {

                int armorType =  3 - armor.armorType;

                allItems[armorType].add(i);

                if (bestArmorDamageReducememt[armorType] < armor.damageReduceAmount) {
                    bestArmorDamageReducememt[armorType] = armor.damageReduceAmount;
                    bestArmorSlots[armorType] = i;
                }
            }

            if (itemStack.getItem() instanceof ItemSword sword) {

                allSwords.add(i);
                int sLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId,itemStack);
                if (bestSwordDamage < sword.getDamageVsEntity()+sLevel) {
                    bestSwordDamage = sword.getDamageVsEntity()+sLevel;
                    bestSwordSlot = i;
                }
            }
        }
        for (int i = 0; i < allItems.length; i++) {
            List<Integer> allItem = allItems[i];
            int finalI = i;
            allItem.stream().filter(slot -> slot != bestArmorSlots[finalI]).forEach(trash::add);
        }
        allSwords.stream().filter(slot -> slot != bestSwordSlot).forEach(trash::add);
    }

    private void searchForItems() {
        bestArmorDamageReducememt = new int[4];
        bestArmorSlots = new int[4];

        Arrays.fill(bestArmorDamageReducememt,-1);
        Arrays.fill(bestArmorSlots,-1);
        bestSwordDamage = -1;
        bestSwordSlot = -1;

        for(int i = 0; i < bestArmorSlots.length; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.armorItemInSlot(i);

            if (itemStack != null && itemStack.getItem() != null) {
                if (itemStack.getItem() instanceof ItemArmor) {
                    ItemArmor armor = (ItemArmor)itemStack.getItem();
                    bestArmorDamageReducememt[i] = armor.damageReduceAmount;
                }
            }
        }

        for (int i = 0; i < 9*4; i++) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);

            if (itemStack == null || itemStack.getItem() == null) continue;

            if (itemStack.getItem() instanceof ItemArmor) {
                ItemArmor armor = (ItemArmor) itemStack.getItem();

                int armorType =  3 - armor.armorType;

                if (bestArmorDamageReducememt[armorType] < armor.damageReduceAmount) {
                    bestArmorDamageReducememt[armorType] = armor.damageReduceAmount;
                    bestArmorSlots[armorType] = i;
                }
            }

            if (itemStack.getItem() instanceof ItemSword) {
                ItemSword sword = (ItemSword) itemStack.getItem();

                if (bestSwordDamage < sword.getDamageVsEntity()) {
                    bestSwordDamage = sword.getDamageVsEntity();
                    bestSwordSlot = i;
                }
            }
        }
    }
}
