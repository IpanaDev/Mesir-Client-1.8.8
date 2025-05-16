package ipana.modules.combat.modes.killaura;

import ipana.events.*;
import ipana.managements.module.Modules;
import ipana.modules.combat.Criticals;
import ipana.modules.combat.KillAura;
import ipana.modules.movement.Speed;
import ipana.modules.movement.modes.fly.LongJump;
import ipana.modules.movement.modes.speed.NCPOnGround;
import ipana.modules.player.AutoPot;
import ipana.utils.math.MathUtils;
import ipana.utils.net.Pinger;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import pisi.unitedmeows.eventapi.event.Event;

public class TickMode extends KaMode {
    public int ticks;
    private int waitTicks;
    private EventPreUpdate pre;
    private boolean usedItem;
    private int swapSlot;
    private boolean yawChange;

    public TickMode(KillAura parent) {
        super("Tick", parent);
    }


    @Override
    public void onEnable() {

        //PlayerUtils.swapBegin();
        //PlayerUtils.swapEnd();
        resetTicks();
        super.onEnable();
    }

    public void bruh(EventExcuseMeWTF event) {
        KillAura ka = getParent();
        if (!ka.targets.isEmpty() && ka.autoBlock.getValue() && ka.canBlock()) {
            ka.block();
        }
    }

    public void onPre(EventPreUpdate event) {
        KillAura ka = getParent();
        if (ka.targets.isEmpty() || ka.curTar == null || !ka.canReach()) {
            resetTicks();
        }
        if (!ka.targets.isEmpty() && ka.curTar != null) {
            if (Modules.CRITICALS.lessDura.getValue() && Modules.CRITICALS.moreDura.getValue()) {
                PlayerUtils.debug("kanka ikisinide açtın napcam.");
            }
            if (!mc.thePlayer.isBlocking() && ka.autoBlock.getValue() && ka.canBlock()) {
                ka.block();
            }
            if (!Modules.AUTO_POT.potting) {
                float[] r = RotationUtils.getRotationsForAura(ka.curTar);
                event.setYaw(r[0]);
                event.setPitch(r[1]);

                if (ka.coolRots.getValue()) {
                    mc.thePlayer.rotationYawHead = event.getYaw();
                    mc.thePlayer.renderYawOffset = event.getYaw();
                    mc.thePlayer.rotationPitchHead = event.getPitch();
                }
            }
            if (ka.canReach()) {
                ticks--;

                setupOrAttack(event);
            }
        }
        yawChange = !yawChange;
    }

    public void onPost(EventPostUpdate event) {
        KillAura ka = getParent();
        if (ka.canReach()) {
            setupOrAttack(event);
            if (ticks <= 0) {
                ticks = 10;
                waitTicks = 0;
            }
        }
    }

    private void setupOrAttack(Event eventBase) {
        if (getParent().improbable.getValue()) {
            improbableAttack(eventBase);
        } else {
            defaultAttack(eventBase);
        }
    }

    private void defaultAttack(Event eventBase) {
        KillAura ka = getParent();
        Speed speed = Modules.SPEED;
        Criticals criticals = Modules.CRITICALS;
        if (eventBase instanceof EventPreUpdate event) {
            hurtTimeWait(3);
            boolean speedCheck = speed.isEnabled() && PlayerUtils.isMoving2();
            boolean ncpOnground = speed.mode.getValue() instanceof NCPOnGround;
            boolean flyCheck = Modules.FLY.isEnabled() && Modules.FLY.mode.getValue() instanceof LongJump;
            if (speedCheck && ncpOnground) {
                if (speed.ticks == 2 && ticks >= 4 && ticks % 2 != 1) {
                    ticks++;
                }
            }

            if (mc.thePlayer.isUsingItem() && !mc.thePlayer.isBlocking()) {
                ticks = 4;
            }

            switch (ticks) {
                case 3 -> {
                    event.setCancelPackets(true);
                    event.setOnGround(true);
                    if (!speedCheck && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.pressed) {
                        event.setY(event.getY() + 0.07);
                    }
                    pre = event;
                }
                case 2 -> {
                    setSlot();
                    ka.curTar.lastAttacked = System.currentTimeMillis();
                    PlayerUtils.swapBegin();
                    if (PlayerUtils.isMoving2()) {
                        ka.unBlock();
                    }
                    PlayerUtils.send(pre.getX(), pre.getY(), pre.getZ(), event.getYaw(), event.getPitch(), pre.isOnGround());
                    mc.thePlayer.report(pre.getX(), pre.getY(), pre.getZ(), event.getYaw(), event.getPitch());
                    hit(ka.curTar);
                    event.setOnGround(false);
                }
                case 1 -> {
                    event.setOnGround(true);
                    if (!speedCheck && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.pressed) {
                        //0.2 offset somehow fixes first attack fight angle in ncp 3.11.1
                        event.setY(event.getY()+0.07);
                    }
                }
                case 0 -> {
                    event.setOnGround(false);
                    if (criticals.moreDura.getValue()) {
                        if (mc.thePlayer.isBlocking()) {
                            ka.unBlock();
                        }
                        PlayerUtils.swapBegin();
                        hit(ka.curTar);
                        PlayerUtils.swapEnd();
                        if (mc.thePlayer.isBlocking()) {
                            ka.reBlock();
                        }
                    }
                    hit(ka.curTar);
                }
            }
        } else if (eventBase instanceof EventPostUpdate) {
            switch (ticks) {
                case 2 -> {
                    hit(ka.curTar);
                    PlayerUtils.swapEnd();
                    if (!criticals.lessDura.getValue()) {
                        hit(ka.curTar);
                    }
                    if (ka.autoBlock.getValue() && ka.canBlock()) {
                        ka.block();
                    }
                }
                case 0 -> {
                    hit(ka.curTar);
                }
            }
        }
    }

    private void improbableAttack(Event eventBase) {
        KillAura ka = getParent();
        Speed speed = Modules.SPEED;
        Criticals criticals = Modules.CRITICALS;
        if (eventBase instanceof EventPreUpdate event) {
            hurtTimeWait(1);
            boolean speedCheck = speed.isEnabled() && PlayerUtils.isMoving2();
            boolean ncpOnground = speed.mode.getValue() instanceof NCPOnGround;
            boolean flyCheck = Modules.FLY.isEnabled() && Modules.FLY.mode.getValue() instanceof LongJump;
            if (speedCheck && ncpOnground) {
                if (speed.ticks == 2 && ticks >= 2 && ticks % 2 != 1) {
                    ticks++;
                }
            }
            if (mc.thePlayer.isUsingItem() && !mc.thePlayer.isBlocking()) {
                ticks = 2;
            }

            switch (ticks) {
                case 1 -> {
                    if (flyCheck) {
                        return;
                    }
                    event.setCancelPackets(true);
                    event.setOnGround(true);
                    if (!speedCheck && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.pressed) {
                        event.setY(event.getY() + 0.07);
                    }
                    pre = event;
                }
                case 0 -> {
                    setSlot();
                    ka.curTar.lastAttacked = System.currentTimeMillis();
                    mc.thePlayer.stopUsingItem();
                    PlayerUtils.swapBegin();
                    ka.unBlock();
                    if (!flyCheck) {
                        PlayerUtils.send(pre.getX(), pre.getY(), pre.getZ(), event.getYaw(), event.getPitch(), pre.isOnGround());
                        mc.thePlayer.report(pre.getX(), pre.getY(), pre.getZ(), event.getYaw(), event.getPitch());
                    }
                    hit(ka.curTar);
                    event.setOnGround(false);
                }
            }
        } else if (eventBase instanceof EventPostUpdate) {
            if (ticks == 0) {
                hit(ka.curTar);
                PlayerUtils.swapEnd();

                if (!criticals.lessDura.getValue()) {
                    hit(ka.curTar);
                }

                PlayerUtils.sendOffset(0, 0.062173, 0, true);

                if (criticals.moreDura.getValue()) {
                    if (mc.thePlayer.isBlocking()) {
                        ka.unBlock();
                    }
                    PlayerUtils.swapBegin();
                    hit(ka.curTar);
                    PlayerUtils.swapEnd();
                    if (mc.thePlayer.isBlocking()) {
                        ka.reBlock();
                    }
                }

                hit(ka.curTar);
                PlayerUtils.sendOffset(0, -5E-4, 0, false);
                hit(ka.curTar);

                if (ka.canBlock() && ka.autoBlock.getValue()) {
                    ka.block();
                }

                PlayerUtils.sendOffset(0, 0, 0, false);
            }
        }
    }
    private void setSlot() {
        if (mc.currentScreen instanceof GuiChest chest) {
            swapSlot = chest.inventoryRows*9;
        } else if (mc.currentScreen instanceof GuiFurnace || mc.currentScreen instanceof GuiRepair) {
            swapSlot = 3;
        } else if (mc.currentScreen instanceof GuiScreenHorseInventory || mc.currentScreen instanceof GuiEnchantment) {
            swapSlot = 2;
        } else if (mc.currentScreen instanceof GuiHopper) {
            swapSlot = 5;
        } else if (mc.currentScreen instanceof GuiCrafting) {
            swapSlot = 10;
        } else {
            swapSlot = 9;
        }
    }

    private void hurtTimeWait(int startTick) {
        KillAura ka = getParent();
        long lag = ka.curTar.lastGenericDamage - ka.curTar.lastAttacked;//Hurt Time delay check
        long timing = System.currentTimeMillis() - ka.curTar.lastGenericDamage + 50;
        if (timing < 0) {
            PlayerUtils.debug("bok mümkün öyle bişi");
        }
        long ping = Pinger.ping();
        long penalty = 1;
        long diff = ping + 50 + penalty;
        if (ticks == startTick && lag > diff && timing+lag < 510) {
            PlayerUtils.debug(String.format("lag: %s, %s, DiffMS=%s, PassTicks=%s", ping, lag, lag-diff, lag/diff));
            ticks += (int) (lag/diff);
        }
    }

    private void hit(EntityLivingBase curTar) {
        //swap   = 0.7   feed
        //sprint = 0.35  feed
        //attack = 2.0   feed
        waitTicks++;
        KillAura ka = getParent();
        boolean block = mc.thePlayer.isBlocking() && getParent().canBlock();
        if (ka.moreKb.getValue()) {
            PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer,C0BPacketEntityAction.Action.START_SPRINTING));
        }
        if (block) {
            getParent().unBlock();
        }
        mc.thePlayer.swingItem();
        PlayerUtils.packet(new C02PacketUseEntity(curTar, C02PacketUseEntity.Action.ATTACK));
        if (block) {
            getParent().reBlock();
        }
        if (getParent().critCrack.getValue() && Modules.CRITICALS.isEnabled()) {
            mc.thePlayer.onCriticalHit(curTar);
            if (PlayerUtils.getEnchantLevel(mc.thePlayer.getHeldItem(), Enchantment.sharpness) > 0) {
                mc.thePlayer.onEnchantmentCritical(curTar);
            }
        }
    }

    @Override
    public void onReceive(EventPacketReceive event) {
        if (event.getState() == EventPacketReceive.PacketState.PRE) {
            if (event.getPacket() instanceof S19PacketEntityStatus status) {
                if (status.getOpCode() == 2 && !getParent().targets.isEmpty() && status.getEntity(mc.theWorld) == getParent().curTar) {
                    //PlayerUtils.debug(System.currentTimeMillis()-lastAttackedMs);
                    //ticks+=hurtDiff;
                    //PlayerUtils.debug(hurtDiff);
                }
            }
        }
        super.onReceive(event);
    }

    @Override
    public void onSend(EventPacketSend event) {

        super.onSend(event);
    }

    private void resetTicks() {
        ticks = getParent().improbable.getValue() ? 2 : 4;
    }
}
