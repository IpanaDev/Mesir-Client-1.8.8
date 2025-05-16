package ipana.modules.combat.modes.killaura;

import ipana.events.EventExcuseMeWTF;
import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Modules;
import ipana.modules.combat.KillAura;
import ipana.modules.movement.Speed;
import ipana.modules.movement.modes.speed.NCPOnGround;
import ipana.utils.net.Pinger;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0BPacketEntityAction;

public class RTE2025 extends KaMode {
    private Minecraft mc = Minecraft.getMinecraft();
    public int ticks;
    private EventPreUpdate prevEvent;
    private boolean recover;
    private boolean oAtaturkunuVarya;
    private int mainSwordSlot;
    private float lastYaw, lastPitch;

    public RTE2025(KillAura parent) {
        super("RTE2025",parent);
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
            ticks = 2;
        }
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
                if (mc.thePlayer.getDistanceToEntity(ka.curTar) <= ka.range.getValue()) {
                    if (mc.thePlayer.ticksExisted % 2 == 0) {
                        mc.thePlayer.swingItem();
                        if (ka.critCrack.getValue() && Modules.CRITICALS.isEnabled()) {
                            mc.thePlayer.onCriticalHit(ka.curTar);
                            if (PlayerUtils.getEnchantLevel(mc.thePlayer.getHeldItem(), Enchantment.sharpness) > 0) {
                                mc.thePlayer.onEnchantmentCritical(ka.curTar);
                            }
                        }
                    }
                    ticks--;
                    hurtAwait();
                    Speed speed = Modules.SPEED;
                    boolean speedCheck = speed.isEnabled() && PlayerUtils.isMoving2();
                    boolean ncpOnground = speed.mode.getValue() instanceof NCPOnGround;
                    if (speedCheck && ncpOnground) {
                        if (ticks == 1 && speed.ticks % 2 == 1) {
                            ticks++;
                        }
                    }
                    if (mc.thePlayer.isUsingItem() && !mc.thePlayer.isBlocking()) {
                        ticks = 2;
                    }
                    if (ticks == 1) {
                        if (mc.thePlayer.isCollidedVertically && !speedCheck) {
                            event.setY(event.getY() + 0.07);
                        }
                        event.setOnGround(true);
                        event.setCancelPackets(true);
                        oAtaturkunuVarya = true;
                    } else if (ticks == 0) {
                        ka.curTar.lastAttacked = System.currentTimeMillis();
                        mainSwordSlot = getEmptySlot();
                        PlayerUtils.swapItem(mainSwordSlot, mc.thePlayer.inventory.currentItem);
                        PlayerUtils.send(prevEvent.getX(), prevEvent.getY(), prevEvent.getZ(), event.getYaw(), event.getPitch(), prevEvent.isOnGround());
                        mc.thePlayer.report(prevEvent.getX(), prevEvent.getY(), prevEvent.getZ(), event.getYaw(), event.getPitch());
                        oAtaturkunuVarya = false;
                        hit(ka.curTar);
                        if (ka.improbable.getValue()) {
                            hit(ka.curTar);
                        }
                        event.setOnGround(false);
                    }
                } else {
                    if (oAtaturkunuVarya && ticks == 1) {
                        event.sendLastPacket(prevEvent);
                        ticks = 2;
                        oAtaturkunuVarya = false;
                    }
                }
            }
            ka.hit = true;
        }
        prevEvent = event;
    }
    public void onPost(EventPostUpdate event) {
        KillAura ka = getParent();
        if (!ka.targets.isEmpty()) {
            if (mc.thePlayer.getDistanceToEntity(ka.curTar) <= ka.range.getValue()) {
                mc.timer.timerSpeed = 1;
                if (ticks == 0) {
                    hit(ka.curTar);
                    PlayerUtils.sendOffset(0, 0.0625, 0, true);
                    int secondSword = getSecondSword();
                    if (secondSword != -1) {
                        PlayerUtils.swapItem(secondSword, mc.thePlayer.inventory.currentItem);
                        hit(ka.curTar);
                    }
                    PlayerUtils.swapItem(mainSwordSlot, mc.thePlayer.inventory.currentItem);
                    hit(ka.curTar);
                    if (secondSword == -1 && Modules.CRITICALS.moreDura.getValue()) {
                        PlayerUtils.swapBegin();
                        PlayerUtils.sendOffset(0, -1E-4, 0, false);
                        PlayerUtils.swapEnd();
                        hit(ka.curTar);
                    }
                    PlayerUtils.sendOffset(0, -1E-4, 0, false);
                    hit(ka.curTar);
                    PlayerUtils.sendOffset(0, 0, 0, false);
                }
                if (ticks <= 0) {
                    ticks = 10;
                }
            }
        }
    }

    public void hurtAwait() {
        KillAura ka = getParent();
        long lag = ka.curTar.lastGenericDamage - ka.curTar.lastAttacked;//Hurt Time delay check
        long timing = System.currentTimeMillis() - ka.curTar.lastGenericDamage + 50;
        if (timing < 0) {
            PlayerUtils.debug("bok mümkün öyle bişi");
        }
        long ping = Pinger.ping();
        long penalty = 1;
        long diff = ping + 50 + penalty;
        if (ticks == 1 && lag > diff && timing+lag < 510) {
            PlayerUtils.debug(String.format("lag: %s, %s, DiffMS=%s, PassTicks=%s", ping, lag, lag-diff, lag/diff));
            ticks += (int) (lag/diff);
        }
    }

    private void hit(EntityLivingBase curTar) {
        KillAura ka = getParent();
        boolean blocking = mc.thePlayer.isBlocking() && ka.canBlock();
        if (ka.moreKb.getValue()) {
            PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer,C0BPacketEntityAction.Action.START_SPRINTING));
        }
        if (blocking) {
            ka.unBlock();
        }
        mc.thePlayer.swingItem();
        PlayerUtils.packet(new C02PacketUseEntity(curTar, C02PacketUseEntity.Action.ATTACK));
        if (blocking) {
            ka.reBlock();
        }
    }

    private int getEmptySlot() {
        for (int i = 9; i < 36; i++) {
            var slot = mc.thePlayer.inventoryContainer.getSlot(i);
            if (!slot.getHasStack()) {
                return i;
            }
            var item = slot.getStack().getItem();
            if (item instanceof ItemSword || item instanceof ItemAxe || item instanceof ItemSpade || item instanceof ItemPickaxe || item instanceof ItemHoe) {
                continue;
            }
            if (PlayerUtils.getEnchantLevel(slot.getStack(), Enchantment.sharpness) > 0) {
                continue;
            }
            return i;
        }
        return -1;
    }

    private int getSecondSword() {
        var main = mc.thePlayer.inventoryContainer.getSlot(mainSwordSlot);

        int mainSharpness = PlayerUtils.getEnchantLevel(main.getStack(), Enchantment.sharpness);
        for (int i = 9; i < 45; i++) {
            var second = mc.thePlayer.inventoryContainer.getSlot(i);
            int secondSharpness = PlayerUtils.getEnchantLevel(second.getStack(), Enchantment.sharpness);
            //sharpness level 1 no crit damage is lower than hand crit damage
            if (secondSharpness > 1 && secondSharpness < mainSharpness) {
                return i;
            }
        }
        return -1;
    }

    public void onDisable() {
        ticks = 2;
    }
    public void onEnable() {
        ticks = 3;
    }
}
