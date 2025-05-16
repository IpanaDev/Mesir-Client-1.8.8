package ipana.modules.player;

import ipana.events.*;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.module.Modules;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.NumberValue;
import ipana.modules.combat.KillAura;
import ipana.modules.movement.Speed;
import ipana.modules.movement.modes.speed.Fantasy;
import ipana.modules.player.strategy.DrinkStrategy;
import ipana.modules.player.strategy.MoveStrategy;
import ipana.utils.player.PlayerUtils;
import net.minecraft.entity.DataWatcher;
import net.minecraft.item.*;
import net.minecraft.network.play.server.S06PacketUpdateHealth;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.Event;
import pisi.unitedmeows.eventapi.event.listener.Listener;

public class AutoDrink extends Module {
    public AutoDrink() {
        super("AutoDrink", Keyboard.KEY_NONE, Category.Player,"Automatically drinks potions.");

        health.setCondition(() -> !drinkStrat.getValue());
    }

    public NumberValue<Integer> health = new NumberValue<>("Health",this,20,0,20,1,"Drink health.");
    public NumberValue<Integer> coolDown = new NumberValue<>("CoolDown",this,3,3,10,1, "CoolDown of swap.");
    public BoolValue cancel = new BoolValue("Cancel",this,false, "Cancel of swap&c03.");
    public BoolValue ataturk = new BoolValue("Ataturk", this, false,"bildigimiz ataturk.");
    public BoolValue c09 = new BoolValue("C09", this, false, "C09 type drink.");
    public BoolValue drinkStrat = new BoolValue("DrinkStrategy", this, false, "Some strategies to improve pvp.");
    public BoolValue moveStrat = new BoolValue("MoveStrategy", this, true, "Move Strategy.", () -> drinkStrat.getValue());
    public boolean swap;
    public int c09Slot;
    public boolean cancelMovement;
    public boolean tooMuchDamage;
    public int cooldown;
    public ItemStack swappedSword, swappedPot;
    private int lagTicks;
    private int deathProbability;
    private DamageSimulation simulation = new DamageSimulation();
    public DrinkStrategy drinkStrategy = new DrinkStrategy();
    public MoveStrategy moveStrategy = new MoveStrategy(this);
    private long healthMs;
    public double lastBlinkX, lastBlinkY, lastBlinkZ;
    private int swapSlot;

    @Override
    public void onEnable() {
        cancelMovement = false;
        swap = false;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    private Listener<EventMoving> onMove = new Listener<EventMoving>(event -> {
        moveStrategy.onMove(event);
    }).weight(Event.Weight.MONITOR);

    private Listener<EventPreUpdate> onPre = new Listener<EventPreUpdate>(event -> {
        if (mc.thePlayer.getHealth() <= 0) {
            swap = false;
            cancelMovement = false;
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            if (cooldown == 0) {
                swap = false;
            }
        }
        ItemStack secondSlot = mc.thePlayer.inventory.mainInventory[1];

        boolean c09Check = c09.getValue() && hotbarPotSlot(Potion.heal) != -1;
        boolean healItem = c09Check || (secondSlot != null && (secondSlot.getItem() instanceof ItemPotion || secondSlot.getItem() instanceof ItemAppleGold));
        //TODO: Simulation and strategy, DrinkStrategy Drink VS Normal Drink
        boolean strategyCheck = drinkStrat.getValue() ? drinkStrategy.shouldDrink(mc.thePlayer.getHealth(), Modules.QUICK_USE.duration.getValue(), healItem) : mc.thePlayer.getHealth() <= health.getValue();

        checkForOtherPotions(Potion.damageBoost);
        checkForOtherPotions(Potion.moveSpeed);

        if (strategyCheck && healItem && hasSword() && cooldown <= 0) {
            lagTicks++;
            if (swap && lagTicks >= 5) {
                PlayerUtils.debug("Slot bug fix (lag?)");
                swap = false;
            }
            if (!swap) {
                swappedSword = mc.thePlayer.getHeldItem();
                swappedPot = secondSlot;
                swapSlot = 37;
                startDrinking(Potion.heal);
                lagTicks = 0;
            }
        }
        event.setCancelPackets(cancelMovement || event.isCancelPackets());
        //simulation.simulateDeath();
    }).filter(filter-> mc.thePlayer.getHeldItem() != null/* || waitPacket > 0*/);

    private Listener<EventPostUpdate> onPost = new Listener<>(event -> {
        moveStrategy.onPost(event);

        if (hasSword()) {
            int seconds = 15;
            if (System.currentTimeMillis()-healthMs >= seconds*1000 + 50) {
                //mc.thePlayer.sendChatMessage("/kit pvpitem");
                healthMs = System.currentTimeMillis();
            }
        }
    });

    private Listener<EventPacketSend> onSend = new Listener<EventPacketSend>(event -> {
        if (event.getState() == EventPacketSend.PacketState.PRE && mc.thePlayer != null) {
            moveStrategy.onSend(event);
        }
    }).weight(Event.Weight.MONITOR);

    private Listener<EventPacketReceive> onReceive = new Listener<>(event -> {
        if (event.getState() == EventPacketReceive.PacketState.PRE && mc.thePlayer != null) {
            if (event.getPacket() instanceof S1CPacketEntityMetadata packet) {
                if (mc.thePlayer.getEntityId() == packet.getEntityId()) {
                    for (DataWatcher.WatchableObject watchableObject : packet.watchableObjects()) {
                        if (watchableObject.getDataValueId() == 6) {
                            float health = (float) watchableObject.getObject();
                            ItemStack drinkSlot = mc.thePlayer.inventory.mainInventory[1];
                            boolean c09Check = c09.getValue() && hotbarPotSlot(Potion.heal) != -1;
                            boolean itemCheck = c09Check || (drinkSlot != null && (drinkSlot.getItem() instanceof ItemPotion || drinkSlot.getItem() instanceof ItemAppleGold));
                            boolean strategyCheck = drinkStrat.getValue() ? drinkStrategy.shouldDrink(health, Modules.QUICK_USE.duration.getValue(), itemCheck) : health <= this.health.getValue();
                            if (strategyCheck && itemCheck) {
                                if (cooldown <= 0 && !swap && hasSword()) {
                                    swappedSword = mc.thePlayer.getHeldItem();
                                    swappedPot = drinkSlot;
                                    swapSlot = 37;
                                    startDrinking(Potion.heal);
                                }
                            }
                        }
                    }
                }
            } else if (event.getPacket() instanceof S2FPacketSetSlot packet) {
                if (packet.itemStack() != null && !c09.getValue()) {
                    ItemStack stack = packet.itemStack();
                    Item item = stack.getItem();
                    if (item instanceof ItemSword && swappedSword != null) {
                        //PlayerUtils.debug("kılıç sıçabilir ",item.getUnlocalizedName(),(packet.slot()-36),mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion,mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword);
                        if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion) {
                            if (packet.slot() - 36 == mc.thePlayer.inventory.currentItem) {
                                packet.setSlot(packet.slot() + 1);
                                PlayerUtils.debug("kılıç sıçtı fixledik aga");
                                //swap = false;
                            }
                        }
                    } else if ((item instanceof ItemPotion || item instanceof ItemAppleGold || item instanceof ItemGlassBottle) && swappedPot != null && stack.getItem() == swappedPot.getItem()) {
                        if (stack.getDisplayName().equals(swappedPot.getDisplayName()) && hasSword() && !swap) {
                            if (packet.slot()-36 == mc.thePlayer.inventory.currentItem) {
                                //packet.setSlot(packet.slot() + 1);
                                PlayerUtils.debug("pot sıçtı fixledik aga");
                            }
                            //PlayerUtils.debug(swap + " : " + packet.slot() + " : " + packet.itemStack().stackSize);
                        }
                    }
                }
            }
        }
    });

    private Listener<EventMoveInput> onInput = new Listener<>(event -> {
        if (cancelMovement) {
            event.setForward(0);
            event.setStrafe(0);
        }
        moveStrategy.onInput(event);
    });

    public void startDrinking(Potion potion) {
        if (c09.getValue()) {
            c09Slot = hotbarPotSlot(potion);
            if (c09Slot == -1) {
                return;
            }
            mc.thePlayer.inventory.currentItem = c09Slot;
            mc.playerController.syncCurrentPlayItem();
        } else {
            PlayerUtils.swapItem(swapSlot, mc.thePlayer.inventory.currentItem);
        }
        swap = true;
        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
        cancelMovement = cancel.getValue();
        mc.gameSettings.keyBindUseItem.pressed = true;
        Modules.QUICK_USE.updateUseTime();
    }
    
    public void resetDrinking() {
        //fixStackSize();
        if (c09.getValue()) {
            mc.thePlayer.inventory.currentItem = 0;
            mc.playerController.syncCurrentPlayItem();
        } else {
            PlayerUtils.swapItem(swapSlot, mc.thePlayer.inventory.currentItem);
        }
        mc.gameSettings.keyBindUseItem.pressed = false;
        KillAura killAura = Modules.KILL_AURA;
        if (killAura.isEnabled() && !killAura.targets.isEmpty() && killAura.autoBlock.getValue() && killAura.canBlock()) {
            killAura.block();
        }
        cooldown = coolDown.getValue();
        cancelMovement = false;
        swap = false;
        if (!c09.getValue()) {
            PlayerUtils.fixInventory();
        }
    }
    public void drinkAgain() {
        KillAura ka = Modules.KILL_AURA;
        Speed speed = Modules.SPEED;
        boolean fantasyCheck = PlayerUtils.isMoving2() && speed.isEnabled() && speed.mode.getValue() instanceof Fantasy;
        boolean speedCheck = (!fantasyCheck || speed.ticks % 2 != 0);
        boolean atam = ataturk.getValue() && !ka.targets.isEmpty() && ka.curTar != null;
        if (!c09.getValue() && atam && this.isEnabled()) {
            PlayerUtils.swapItem(37, mc.thePlayer.inventory.currentItem);
            //PlayerUtils.sendOffset(0, 0.0625, 0, false);
            //PlayerUtils.sendOffset(0, 0, 0, false);
            //PlayerUtils.sendOffset(0, 1E-4, 0, false);
            if (speedCheck) {
                PlayerUtils.sendOffset(0, 0, 0, false);
            }
            ka.basicAttack(ka.curTar);
            PlayerUtils.swapItem(37, mc.thePlayer.inventory.currentItem);
        }
        mc.gameSettings.keyBindUseItem.pressed = true;
        mc.playerController.sendUseItem(mc.thePlayer,mc.theWorld,mc.thePlayer.getHeldItem());
        Modules.QUICK_USE.updateUseTime();
        cancelMovement = cancel.getValue();
        PlayerUtils.debug("pov damage yiyon");
        //PlayerUtils.fixInventory();
    }

    private void checkForOtherPotions(Potion potion) {
        if (!swap && cooldown <= 0 && hasSword() && mc.thePlayer.getHealth() > 15) {
            if (!mc.thePlayer.isPotionActive(potion)) {
                int potSlot = potSlot(potion);
                if (potSlot != -1) {
                    swappedSword = mc.thePlayer.getHeldItem();
                    swappedPot = mc.thePlayer.inventory.mainInventory[potSlot];
                    //swapSlot = (4 - potSlot / 9) * 9 + potSlot % 9;
                    swapSlot = potSlot < 9 ? potSlot + 36 : potSlot;
                    PlayerUtils.debug("Drinking "+swappedPot.getDisplayName()+" potion.");
                    startDrinking(potion);
                }
            }
        }
    }


    private int hotbarPotSlot(Potion wanted) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemPotion potion) {
                for (PotionEffect effect : potion.getEffects(stack)) {
                    if (effect.getPotionID() == wanted.id) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
    private int potSlot(Potion wanted) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemPotion potion) {
                for (PotionEffect effect : potion.getEffects(stack)) {
                    if (effect.getPotionID() == wanted.id) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    private boolean hasSword() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword;
    }

    public void onKeyCheck() {
        if (isEnabled() && swap && !mc.gameSettings.keyBindUseItem.pressed) {
            boolean potionInHand = mc.thePlayer.getHeldItem() != null && (mc.thePlayer.getHeldItem().getItem() instanceof ItemPotion || mc.thePlayer.getHeldItem().getItem() instanceof ItemAppleGold);
            if (potionInHand) {
                mc.gameSettings.keyBindUseItem.pressed = true;
            }
        }
    }
}
