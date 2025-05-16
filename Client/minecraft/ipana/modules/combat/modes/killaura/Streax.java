package ipana.modules.combat.modes.killaura;

import ipana.events.EventExcuseMeWTF;
import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Modules;
import ipana.modules.combat.KillAura;
import ipana.utils.math.WindowedIterator;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Streax extends KaMode {
    private int ticks;
    private List<ItemSlot> itemSlots = new ArrayList<>();

    public Streax(KillAura parent) {
        super("Streax",parent);
    }

    public void bruh(EventExcuseMeWTF event) {
        KillAura ka = getParent();
        if (!ka.targets.isEmpty() && ka.autoBlock.getValue() && ka.canBlock()) {
            ka.block();
        }
    }


    public void onPre(EventPreUpdate event) {
        KillAura ka = getParent();
        if (!ka.targets.isEmpty()) {
            if (!mc.thePlayer.isBlocking() && ka.autoBlock.getValue() && ka.canBlock()) {
                ka.block();
            }
            if (!Modules.AUTO_POT.potting) {
                float[] rots = RotationUtils.getRotationsForAura(ka.curTar);
                float yaw = rots[0];
                float pitch = rots[1];
                event.setYaw(yaw);
                event.setPitch(pitch);
                if (ka.coolRots.getValue()) {
                    mc.thePlayer.rotationYawHead = event.getYaw();
                    mc.thePlayer.renderYawOffset = event.getYaw();
                    mc.thePlayer.rotationPitchHead = event.getPitch();
                }
            }
        }
    }

    public void onPost(EventPostUpdate event) {
        KillAura ka = getParent();
        if (!ka.targets.isEmpty() && ka.canReach()) {
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                mc.thePlayer.swingItem();
            }
            ticks--;
            if (ticks <= 0) {
                boolean block = (mc.thePlayer.isBlocking() && ka.canBlock());
                if (block) {
                    ka.unBlock();
                }
                PlayerUtils.swapItem(9, mc.thePlayer.inventory.currentItem);
                setup();
                int swapCount = 0;
                float lastDamage = 0;
                for (var item : itemSlots) {
                    double diff = item.damage - lastDamage;
                    //PlayerUtils.debug(diff);
                    lastDamage = item.damage;
                    if (item.enchSlot == -1) {
                        item.swap();
                        item.packet();
                        item.swap();
                        hit();
                        swapCount += 2;
                    } else if (item.slot != item.enchSlot) {
                        item.swap();
                        item.packet();
                        item.swapEnch();
                        hit();
                        item.swapEnch();
                        item.swap();
                        swapCount += 4;
                    } else {
                        item.swap();
                        item.packet();
                        hit();
                        item.swap();
                        swapCount += 2;
                    }
                }
                PlayerUtils.swapItem(9, mc.thePlayer.inventory.currentItem);
                if (block) {
                    ka.reBlock();
                }
                PlayerUtils.fixInventory();
                //PlayerUtils.debug("Damages: "+itemSlots.size()+"/"+swapCount+" : "+ticks);
                if (ticks <= 0) {
                    ticks = 12;
                }
            }
        } else {
            ticks = 0;
        }
    }

    private void setup() {
        itemSlots.clear();
        List<ItemSlot> enchDamages = new ArrayList<>();
        List<ItemSlot> normalDamages = new ArrayList<>();
        float strengthDamage = 1.3f * mc.thePlayer.getActivePotionLevel(Potion.damageBoost);
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            float baseDamage = 1f;
            int swapSlot = i < 9 ? i + 36 : i;
            if (stack == null) {
                baseDamage *= 1 + strengthDamage;
                if (!hasSameDamages(normalDamages, baseDamage)) {
                    normalDamages.add(new ItemSlot(baseDamage, false, swapSlot, -1));
                }
                if (!hasSameDamages(normalDamages, baseDamage * 1.5f)) {
                    normalDamages.add(new ItemSlot(baseDamage * 1.5f, true, swapSlot, -1));
                }
                continue;
            }
            float toolDamage = 0;
            if (stack.getItem() instanceof ItemSword sword) {
                toolDamage = sword.attackDamage;
            } else if (stack.getItem() instanceof ItemTool tool) {
                toolDamage = tool.damageVsEntity;
            }
            baseDamage += toolDamage;
            baseDamage *= 1 + strengthDamage;
            int enchLvl = PlayerUtils.getEnchantLevel(stack, Enchantment.sharpness);
            float enchDamage = 1.25f * enchLvl;
            if (!hasSameDamages(enchDamages, enchDamage)) {
                enchDamages.add(new ItemSlot(enchDamage, false, swapSlot, 0));
            }
            if (!hasSameDamages(normalDamages, baseDamage)) {
                normalDamages.add(new ItemSlot(baseDamage, false, swapSlot, -1));
            }
            if (!hasSameDamages(normalDamages, baseDamage * 1.5f)) {
                normalDamages.add(new ItemSlot(baseDamage * 1.5f, true, swapSlot, -1));
            }
        }
        for (var normal : normalDamages) {
            if (!hasSameDamages(itemSlots, normal.damage)) {
                itemSlots.add(new ItemSlot(normal.damage, normal.crit, normal.slot, normal.enchSlot));
            }
            for (var ench : enchDamages) {
                if (!hasSameDamages(itemSlots, normal.damage + ench.damage)) {
                    itemSlots.add(new ItemSlot(normal.damage + ench.damage, normal.crit, normal.slot, ench.slot));
                }
            }
        }
        itemSlots.sort(Comparator.comparing(f -> f.damage));
    }

    private boolean hasSameDamages(List<ItemSlot> itemSlots, float damage) {
        for (var item : itemSlots) {
            if (item.damage == damage) {
                return true;
            }
        }
        return false;
    }


    private void hit() {
        KillAura ka = getParent();
        if (ka.moreKb.getValue()) {
            PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
        }

        mc.thePlayer.swingItem();
        PlayerUtils.packet(new C02PacketUseEntity(ka.curTar, C02PacketUseEntity.Action.ATTACK));

        if (ka.critCrack.getValue() && Modules.CRITICALS.isEnabled()) {
            mc.thePlayer.onCriticalHit(ka.curTar);
            if (PlayerUtils.getEnchantLevel(mc.thePlayer.getHeldItem(), Enchantment.sharpness) > 0) {
                mc.thePlayer.onEnchantmentCritical(ka.curTar);
            }
        }
    }

    public class ItemSlot {
        public float damage;
        public int slot;
        public int enchSlot;
        public boolean crit;

        public ItemSlot(float damage, boolean crit, int slot, int enchSlot) {
            this.damage = damage;
            this.crit = crit;
            this.slot = slot;
            this.enchSlot = enchSlot;
        }

        private void packet() {
            if (crit) {
                PlayerUtils.sendOffset(0, 1E-4, 0, true);
                PlayerUtils.sendOffset(0, 0, 0, false);
            } else {
                PlayerUtils.sendOffset(0, 0, 0, true);
            }
        }
        public void swap() {
            PlayerUtils.swapItem(slot, mc.thePlayer.inventory.currentItem);
        }
        public void swapEnch() {
            PlayerUtils.swapItem(enchSlot, mc.thePlayer.inventory.currentItem);
        }
    }

    public void onDisable() {
        KillAura ka = getParent();
        ka.hit = false;
    }
    public void onEnable() {
        KillAura ka = getParent();
        ka.hit = false;
    }
}
