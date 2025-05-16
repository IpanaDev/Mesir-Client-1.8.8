package ipana.modules.player;

import ipana.events.*;
import ipana.managements.value.values.EnumValue;
import ipana.modules.combat.KillAura;
import net.minecraft.network.play.client.C03PacketPlayer;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.managements.value.Value;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.ModeValue;
import ipana.managements.value.values.NumberValue;
import ipana.utils.Timer;
import ipana.utils.player.PlayerUtils;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;

import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.util.ArrayList;
import java.util.List;

public class ArmorSwap extends Module {
    public ArmorSwap() {
        super("ArmorSpoof", Keyboard.KEY_NONE, Category.Exploit,"Automatically swaps to best armor.");
    }
    private NumberValue<Integer> delay = new NumberValue<>("Delay",this,0,0,20,1,"Swap delay.");
    private EnumValue<ArmorType> type = new EnumValue<>("Type",this,ArmorType.class,"Swap type.");
    private BoolValue fixall = new BoolValue("FixAll",this,false,"Fix all.");
    private BoolValue helmet = new BoolValue("Helmet",this,false,"eymenin beyinsiz kafası.");
    private BoolValue chest = new BoolValue("Chestplate",this,false,"eymenin memeleri.");
    private BoolValue leg = new BoolValue("Leggings",this,false,"eymenin götü.");
    private BoolValue boots = new BoolValue("Boots",this,false,"adidas giyen keko eymen.");
    private int ticks;
    private boolean helmetSwapped,chestSwapped,legSwapped,bootSwapped;
    private int swapIndex;
    private EventPreUpdate pre;
    private List<C03PacketPlayer> packets = new ArrayList<>();
    private Timer timer = new Timer();

    private Listener<EventPreUpdate> onPre = new Listener<>(event -> {
        if (type.getValue() == ArmorType.ARMOR2) {
            ItemStack[] inv = mc.thePlayer.getInventory();

            boolean helm = mc.thePlayer.inventory.getStackInSlot(5) != null && mc.thePlayer.inventory.getStackInSlot(5).getItem() != null && mc.thePlayer.inventory.getStackInSlot(5).getItem() instanceof ItemArmor;
            boolean chest = mc.thePlayer.inventory.getStackInSlot(6) != null && mc.thePlayer.inventory.getStackInSlot(6).getItem() != null && mc.thePlayer.inventory.getStackInSlot(6).getItem() instanceof ItemArmor;
            boolean leg = mc.thePlayer.inventory.getStackInSlot(7) != null && mc.thePlayer.inventory.getStackInSlot(7).getItem() != null && mc.thePlayer.inventory.getStackInSlot(7).getItem() instanceof ItemArmor;
            boolean boot = mc.thePlayer.inventory.getStackInSlot(8) != null && mc.thePlayer.inventory.getStackInSlot(8).getItem() != null && mc.thePlayer.inventory.getStackInSlot(8).getItem() instanceof ItemArmor;

            int helmValue = helm ? (mc.thePlayer.inventory.getStackInSlot(5).getMaxDamage() - mc.thePlayer.inventory.getStackInSlot(5).getItemDamage()) : 0;
            int chestValue = chest ? (mc.thePlayer.inventory.getStackInSlot(6).getMaxDamage() - mc.thePlayer.inventory.getStackInSlot(6).getItemDamage()) : 0;
            int legsValue = leg ? (mc.thePlayer.inventory.getStackInSlot(7).getMaxDamage() - mc.thePlayer.inventory.getStackInSlot(7).getItemDamage()) : 0;
            int bootValue = boot ? (mc.thePlayer.inventory.getStackInSlot(8).getMaxDamage() - mc.thePlayer.inventory.getStackInSlot(8).getItemDamage()) : 0;


            int curHelmValue = inv[3] != null ? (inv[3].getMaxDamage() - inv[3].getItemDamage()) : 0;
            int curChestValue = inv[2] != null ? (inv[2].getMaxDamage() - inv[2].getItemDamage()) : 0;
            int curLegsValue = inv[1] != null ? (inv[1].getMaxDamage() - inv[1].getItemDamage()) : 0;
            int curBootValue = inv[0] != null ? (inv[0].getMaxDamage() - inv[0].getItemDamage()) : 0;
            if (ticks <= 0) {
                if (helm) {
                    if (helmValue > curHelmValue) {
                        PlayerUtils.swapItem(5, 5);
                    }
                }
                if (chest) {
                    if (chestValue > curChestValue) {
                        PlayerUtils.swapItem(6, 6);
                    }
                }
                if (leg) {
                    if (legsValue > curLegsValue) {
                        PlayerUtils.swapItem(7, 7);
                    }
                }
                if (boot) {
                    if (bootValue > curBootValue) {
                        PlayerUtils.swapItem(8, 8);
                    }
                }
                ticks = delay.getValue();
            } else {
                ticks--;
            }
        } else {
            if (ticks == 1) {
                boolean willSwap = helmetSwapped || chestSwapped || legSwapped || bootSwapped;
                if (helmetSwapped) {
                    PlayerUtils.swapItem(5, 5,2173);
                    helmetSwapped = false;
                }
                if (chestSwapped) {
                    PlayerUtils.swapItem(6, 6,2173);
                    chestSwapped = false;
                }
                if (legSwapped) {
                    PlayerUtils.swapItem(7, 7,2173);
                    legSwapped = false;
                }
                if (bootSwapped) {
                    PlayerUtils.swapItem(8, 8,2173);
                    bootSwapped = false;
                }
                if (willSwap) {
                    KillAura ka = Modules.KILL_AURA;
                    if (ka.canBlock() && !PlayerUtils.isMoving2()) {
                        ka.reBlock();
                    }
                }
                //PlayerUtils.packet(pre.getPacket());
                for (C03PacketPlayer packetPlayer : packets) {
                    if (packetPlayer != null) {
                        PlayerUtils.packet(packetPlayer);
                    }
                }
                packets.clear();
                ticks = -1;
            }
        }
        if (timer.delay(31000) && fixall.getValue()) {
            mc.thePlayer.sendChatMessage("/fix all");
            timer.reset();
        }
    });

    private Listener<EventPostUpdate> onPost = new Listener<EventPostUpdate>(event -> {
        if (mc.thePlayer.hurtResistantTime == 12) {
            ticks = 0;
        }
        if (ticks == 0) {
            if (swapIndex == 0) {
                if (helmet.getValue()) {
                    helmetSwapped = true;
                    PlayerUtils.swapItem(5, 5);
                    PlayerUtils.swapItem(5, 5, 502);
                } else {
                    swapIndex++;
                }
            }
            if (swapIndex == 1) {
                if (chest.getValue()) {
                    chestSwapped = true;
                    PlayerUtils.swapItem(6, 6);
                    PlayerUtils.swapItem(6, 6, 502);
                } else {
                    swapIndex++;
                }
            }
            if (swapIndex == 2) {
                if (leg.getValue()) {
                    legSwapped = true;
                    PlayerUtils.swapItem(7, 7);
                    PlayerUtils.swapItem(7, 7, 502);
                } else {
                    swapIndex++;
                }
            }
            if (swapIndex == 3) {
                if (boots.getValue()) {
                    bootSwapped = true;
                    PlayerUtils.swapItem(8, 8);
                    PlayerUtils.swapItem(8, 8, 502);
                }
            }
            if (helmetSwapped || chestSwapped || legSwapped || bootSwapped) {
                KillAura ka = Modules.KILL_AURA;
                if (!ka.hit && ka.canBlock()) {
                    ka.reBlock();
                }
            }
            swapIndex++;
            if (swapIndex >= getArmorIndex())
                swapIndex = 0;
            ticks = 10;
        }
    }).filter(filter -> type.getValue() == ArmorType.ARMOR1 && Modules.KILL_AURA.canBlock());

    private Listener<EventPacketSend> onSend = new Listener<EventPacketSend>(event -> {
        if (event.getPacket() instanceof C03PacketPlayer player) {
            if (ticks > 1 && ticks < 10) {
                event.setCancelled(true);
                packets.add(player);
                ticks--;
            }
            if (ticks == 10) {
                event.setCancelled(true);
                packets.add(player);
                ticks = 2;
            }
        }
    }).filter(filter -> type.getValue() == ArmorType.ARMOR1 && Modules.KILL_AURA.canBlock());

    private int getArmorIndex() {
        int i = -1;
        if (helmet.getValue()) {
            i = 1;
        }
        if (chest.getValue()) {
            i = 2;
        }
        if (leg.getValue()) {
            i = 3;
        }
        if (boots.getValue()) {
            i = 4;
        }
        return i;
    }
    @Override
    public void onSuffixChange() {
        setSuffix(type.getValue().enumName());
        super.onSuffixChange();
    }
    @Override
    public String getDescription() {
        return "Swaps To Best-Durability Armor (Need 2 Armors)";
    }

    enum ArmorType {
        ARMOR1("1Armor"),
        ARMOR2("2Armor");
        private String enumName;

        ArmorType(String enumName) {
            this.enumName = enumName;
        }

        public String enumName() {
            return enumName;
        }
    }
}
