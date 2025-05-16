package ipana.modules.combat.modes.killaura;


import ipana.events.EventExcuseMeWTF;
import ipana.events.EventPostUpdate;
import ipana.events.EventPreUpdate;
import ipana.managements.module.Modules;
import ipana.modules.combat.KillAura;
import ipana.modules.player.AutoPot;
import ipana.utils.net.Pinger;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import pisi.unitedmeows.eventapi.event.Event;

import java.util.Comparator;
import java.util.function.Consumer;

public class HurtTimeMode extends KaMode {

    public int ticks;
    private EventPreUpdate preEvent;
    private float startDamage;
    private float enchantless;
    private long ms;
    private Method method;

    public HurtTimeMode(KillAura parent) {
        super("HurtTime", parent);
    }

    @Override
    public void onEnable() {
        ticks = 11;
        super.onEnable();
    }

    @Override
    public void bruh(EventExcuseMeWTF event) {
        KillAura ka = getParent();
        if (!ka.targets.isEmpty()) {
            if (ka.autoBlock.getValue() && ka.canBlock()) {
                ka.block();
            }
        }
        super.bruh(event);
    }

    @Override
    public void onPre(EventPreUpdate event) {
        KillAura ka = getParent();
        if (!ka.targets.isEmpty()) {
            if (!mc.thePlayer.isBlocking() && ka.autoBlock.getValue() && ka.canBlock()) {
                ka.block();
            }

            if (!Modules.AUTO_POT.potting) {
                float[] rotations = RotationUtils.getRotationsForAura(ka.curTar);
                event.setYaw(rotations[0]);
                event.setPitch(rotations[1]);
                if (ka.coolRots.getValue()) {
                    mc.thePlayer.rotationYawHead = event.getYaw();
                    mc.thePlayer.renderYawOffset = event.getYaw();
                    mc.thePlayer.rotationPitchHead = event.getPitch();
                }
            }
            if (mc.thePlayer.getDistanceToEntity(ka.curTar) <= ka.range.getValue()) {
                if (ka.curTar.hurtResistantTime > 13) {
                    if (ticks == 10) {
                        ticks++;
                        //TODO
                        //PlayerUtils.debug("Hurt Wait: "+ka.curTar.hurtResistantTime);
                    }
                }
                ticks--;
                setupDamages(event);

                mc.thePlayer.swingItem();
                ms = 1;

                Setup<EventPreUpdate> preSetup = new Setup<>(event, ticks, mc.thePlayer, preEvent);
                method.preConsumer.accept(preSetup);
            }
        } else {
            ticks = 11;
        }
        preEvent = event;
        super.onPre(event);
    }


    @Override
    public void onPost(EventPostUpdate event) {
        KillAura ka = getParent();
        if (!ka.targets.isEmpty()) {
            if (mc.thePlayer.getDistanceToEntity(ka.curTar) <= ka.range.getValue()) {
                Setup<EventPostUpdate> postSetup = new Setup<>(event, ticks, mc.thePlayer, preEvent);
                method.postConsumer.accept(postSetup);
                if (ticks<=0) {
                    ticks=ka.dura.getValue() ? 11 : 12;
                }
            }
        }
        super.onPost(event);
    }

    private void hitEnchantless() {
        KillAura ka = getParent();
        boolean block = mc.thePlayer.isBlocking();
        if (block) {
            ka.unBlock();
        }
        PlayerUtils.swapBegin();
        hit(ka.curTar);
        PlayerUtils.swapEnd();
        if (block) {
            ka.reBlock();
        }
    }

    private void hit(EntityLivingBase ent) {
        KillAura ka = getParent();
        boolean block = mc.thePlayer.isBlocking() && ka.canBlock();
        if (ka.moreKb.getValue()) {
            PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer,C0BPacketEntityAction.Action.STOP_SPRINTING));
            PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer,C0BPacketEntityAction.Action.START_SPRINTING));
        }
        if (ms > 1) {
            mc.thePlayer.swingItem();
        }
        if (block) {
            ka.unBlock();
        }
        //boolean fightSpeed = mc.thePlayer.fightSpeed.check(System.currentTimeMillis(), 50);
        //if (fightSpeed) {
            //PlayerUtils.debug("fight speed yiyom");
        //}
        PlayerUtils.packet(new C02PacketUseEntity(ent, C02PacketUseEntity.Action.ATTACK));

        if (block) {
            ka.reBlock();
        }
        if (ka.critCrack.getValue() && Modules.CRITICALS.isEnabled()) {
            mc.thePlayer.onCriticalHit(ent);
            if (PlayerUtils.getEnchantLevel(mc.thePlayer.getHeldItem(), Enchantment.sharpness) > 0) {
                mc.thePlayer.onEnchantmentCritical(ent);
            }
        }
        ms++;
    }

    private void setupDamages(EventPreUpdate event) {
        if (ticks == 10) {
            if (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword tool) {
                enchantless = 5+tool.getDamageVsEntity();
                if (mc.thePlayer.getActivePotionEffect(Potion.damageBoost) != null) {
                    enchantless *= 1.3f * (mc.thePlayer.getActivePotionEffect(Potion.damageBoost).getAmplifier() + 1);
                }
                startDamage = (float) (enchantless+(1.25*PlayerUtils.getEnchantLevel(mc.thePlayer.getHeldItem(), Enchantment.sharpness)));
            }
            event.setOnGround(true);
        }
        float critDamage = enchantless/2;
        if (startDamage-enchantless+1 > enchantless && enchantless + critDamage > startDamage-enchantless+1+0.5) {
            method = Method.MODE_4;
        } else if (startDamage-enchantless+1 > enchantless+critDamage && startDamage-enchantless+1 > enchantless && startDamage-enchantless+1+0.5 > enchantless + critDamage) {
            method = Method.MODE_2;
        } else if (enchantless + critDamage > startDamage && startDamage+critDamage > enchantless + critDamage) {
            method = Method.MODE_3;
        } else if (startDamage-enchantless+1 < enchantless + critDamage && startDamage-enchantless+1+0.5 > enchantless + critDamage) {
            if (ticks == 7) {
                event.setCancelPackets(true);
            }
            method = Method.MODE_2;
        } else if (enchantless > startDamage-enchantless+1 && startDamage-enchantless+1+0.5 > enchantless) {
            method = Method.MODE_1;
        } else {
            method = Method.MODE_0;
        }
        //PlayerUtils.debug(method);
    }

    private void hurtTimeWait() {
        KillAura ka = getParent();
        long lag = ka.curTar.lastGenericDamage - ka.curTar.lastAttacked;//Hurt Time delay check
        long timing = System.currentTimeMillis() - ka.curTar.lastGenericDamage + 50;
        if (timing < 0) {
            PlayerUtils.debug("bok mümkün öyle bişi");
        }
        long ping = Pinger.ping();
        long penalty = 1;
        long diff = ping + 50 + penalty;
        if (ticks == 3 && lag > diff && timing+lag < 510) {
            PlayerUtils.debug(String.format("lag: %s, %s, %s, %s", ping, lag, lag-diff, lag/diff));
            ticks+= (int) (lag/diff);
        }
    }

    enum Method {

        MODE_0(preSetup -> {
            switch (preSetup.ticks) {
                case 9,5,3,1 -> {
                    if (preSetup.ticks == 9) {
                        preSetup.cancelPackets();
                    }
                    preSetup.crit();
                }
                case 8,7,4,2,0 -> {
                    if (preSetup.ticks == 8 || preSetup.ticks == 4) {
                        PlayerUtils.swapBegin();
                        if (preSetup.ticks == 8) {
                            preSetup.ka().unBlock();
                            preSetup.event.sendLastPacket(preSetup.last);
                        }
                    }
                    if (Modules.CRITICALS.moreDura.getValue() && preSetup.ticks == 2) {
                        preSetup.hitEnchantless();
                    }
                    if (preSetup.ticks == 8 || preSetup.ticks == 7) {
                        preSetup.hit();
                    }
                    preSetup.event.setOnGround(false);
                }
            }
        }, postSetup -> {
            switch (postSetup.ticks) {
                case 8 -> {
                    postSetup.hit();
                    PlayerUtils.skipTicks();
                    PlayerUtils.swapEnd();
                    postSetup.ka().reBlock();
                    //postSetup.hit(); block may be required
                }
                case 4 -> {
                    PlayerUtils.swapEnd();
                    postSetup.hit();
                }
                case 2 -> postSetup.hitEnchantless();
                case 1,0 -> postSetup.hit();
            }
        }),

        MODE_1(preSetup -> {
            switch (preSetup.ticks) {
                case 9,1,5,3 -> preSetup.crit();
                case 8,0,4,2 -> {
                    if (preSetup.ticks == 8 || preSetup.ticks == 4) {
                        PlayerUtils.swapBegin();
                        preSetup.event.sendLastPacket(preSetup.last);
                    }
                    if (preSetup.ticks == 8 || preSetup.ticks == 0) {
                        preSetup.hit();
                    }
                    preSetup.event.setOnGround(false);
                }
            }
        }, postSetup -> {
            switch (postSetup.ticks) {
                case 8 -> {
                    postSetup.hit();
                    PlayerUtils.skipTicks();
                    PlayerUtils.swapEnd();
                    postSetup.hit();
                }
                case 4 -> {
                    PlayerUtils.swapEnd();
                    postSetup.hit();
                }
                case 2 -> postSetup.hitEnchantless();
                case 0 -> postSetup.hit();
            }
        }),

        MODE_2(preSetup -> {
            switch (preSetup.ticks) {
                case 8,1,3 -> {
                    if (preSetup.ticks == 8) {
                        preSetup.event.setCancelPackets(true);
                    }
                    preSetup.crit();
                }
                case 7,0,4,2 -> {
                    if (preSetup.ticks == 7 || preSetup.ticks == 0) {
                        if (preSetup.ticks == 7) {
                            PlayerUtils.swapBegin();
                            preSetup.event.sendLastPacket(preSetup.last);
                        }
                        preSetup.hit();
                    } else if (preSetup.ticks == 4) {
                        preSetup.ka().unBlock();
                        PlayerUtils.swapBegin();
                        preSetup.hit();
                    } else {
                        PlayerUtils.swapBegin();
                    }
                    preSetup.event.setOnGround(false);
                }
                case 5 -> preSetup.event.setOnGround(true);
            }
        }, postSetup -> {
            switch (postSetup.ticks) {
                case 7 -> {
                    postSetup.hit();
                    PlayerUtils.swapEnd();
                }
                case 4, 2 -> {
                    PlayerUtils.swapEnd();
                    postSetup.hit();
                }
                case 0 -> postSetup.hit();
            }
        }),

        MODE_3(preSetup -> {
            switch (preSetup.ticks) {
                case 8,1,5 -> {
                    preSetup.crit();
                    if (preSetup.ticks == 8) {
                        preSetup.cancelPackets();
                    }
                }
                case 7,0,4 -> {
                    if (preSetup.ticks == 7 || preSetup.ticks == 4) {
                        PlayerUtils.swapBegin();
                        if (preSetup.ticks == 7) {
                            preSetup.ka().unBlock();
                            preSetup.event.sendLastPacket(preSetup.last);
                            preSetup.hit();
                        }
                    }
                    preSetup.event.setOnGround(false);
                }
                case 2 -> preSetup.event.setOnGround(true);
            }
        }, postSetup -> {
            switch (postSetup.ticks) {
                case 7 -> {
                    postSetup.hit();
                    PlayerUtils.skipTicks();
                    PlayerUtils.swapEnd();
                    postSetup.hit();
                }
                case 4 -> {
                    PlayerUtils.swapEnd();
                    postSetup.hit();
                }
                case 2 -> postSetup.hit();
                case 0 ->  {
                    postSetup.hitEnchantless();
                    postSetup.hit();
                }
            }
        }),

        MODE_4(preSetup -> {
            switch (preSetup.ticks) {
                case 8,3,1 -> {
                    preSetup.crit();
                    if (preSetup.ticks == 8) {
                        preSetup.cancelPackets();
                    }
                }
                case 7,2,0 -> {
                    if (preSetup.ticks == 7 || preSetup.ticks == 2) {
                        PlayerUtils.swapBegin();
                        if (preSetup.ticks == 7) {
                            preSetup.ka().unBlock();
                            preSetup.event.sendLastPacket(preSetup.last);
                        }
                    }
                    if (preSetup.ticks == 7 || preSetup.ticks == 0) {
                        preSetup.hit();
                    }
                    preSetup.event.setOnGround(false);
                }
                case 4 -> {
                    preSetup.event.setOnGround(true);
                }
            }
        }, postSetup -> {
            switch (postSetup.ticks) {
                case 7 -> {
                    postSetup.hit();
                    PlayerUtils.swapEnd();
                    postSetup.ka().reBlock();
                }
                case 4 -> {
                    postSetup.ka().unBlock();
                    PlayerUtils.swapBegin();
                    postSetup.hit();
                    PlayerUtils.skipTicks();
                    PlayerUtils.swapEnd();
                    postSetup.hit();
                }
                case 2 -> {
                    PlayerUtils.swapEnd();
                    postSetup.hit();
                }
                case 0 -> postSetup.hit();
            }
        });

        Consumer<Setup<EventPreUpdate>> preConsumer;
        Consumer<Setup<EventPostUpdate>> postConsumer;

        Method(Consumer<Setup<EventPreUpdate>> preConsumer, Consumer<Setup<EventPostUpdate>> postConsumer) {
            this.preConsumer = preConsumer;
            this.postConsumer = postConsumer;
        }
    }

    class Setup<T extends Event> {
        private T event;
        private int ticks;
        private EntityPlayerSP player;
        private EventPreUpdate last;

        Setup(T event, int ticks, EntityPlayerSP thePlayer, EventPreUpdate last) {
            this.event = event;
            this.ticks = ticks;
            this.player = thePlayer;
            this.last = last;
        }

        KillAura ka() {
            return getParent();
        }

        void hit() {
            HurtTimeMode.this.hit(ka().curTar);
        }

        void hitEnchantless() {
            HurtTimeMode.this.hitEnchantless();
        }

        void crit() {
            if (event instanceof EventPreUpdate pre && (!Modules.SPEED.isEnabled() || !PlayerUtils.isMoving2()) && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.pressed) {
                pre.setY(pre.getY()+0.07);
                pre.setOnGround(true);
            }
        }
        void cancelPackets() {
            if (event instanceof EventPreUpdate pre) {
                pre.setCancelPackets(true);
            }
        }
    }
}