package ipana.modules.combat.modes.killaura;

import ipana.events.*;
import ipana.managements.module.Modules;
import ipana.modules.combat.Criticals;
import ipana.modules.combat.KillAura;
import ipana.modules.combat.modes.criticals.Packets;
import ipana.modules.movement.Fly;
import ipana.modules.movement.Speed;
import ipana.modules.movement.modes.fly.LongJump;
import ipana.modules.movement.modes.fly.OldNCPGlide;
import ipana.modules.movement.modes.speed.Fantasy;
import ipana.modules.movement.modes.speed.NCPOnGround;
import ipana.modules.movement.modes.speed.OldNCP;
import ipana.modules.player.AutoPot;
import ipana.modules.player.modes.autopot.JumpPot;
import ipana.utils.net.Pinger;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Timer;

import java.util.ArrayList;

public class Single extends KaMode {
    private Minecraft mc = Minecraft.getMinecraft();
    private ArrayList<Long> aps = new ArrayList<>();
    private float lastYaw,lastPitch;
    private int attackTicks;
    private boolean crittedForSure;

    public Single(KillAura parent) {
        super("Single",parent);
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
            if (ka.canReach() && !isSpeedActive(true)) {
                ka.hit = !ka.hit;
                defaultCrit(event);
            }
            if (!Modules.AUTO_POT.potting) {
                float[] rots = RotationUtils.getRotationsForAura(ka.curTar);
                float yaw = rots[0];
                float pitch = rots[1];

                event.setYaw(yaw);
                event.setPitch(pitch);
                lastYaw = event.getYaw();
                lastPitch = event.getPitch();
                if (ka.coolRots.getValue()) {
                    mc.thePlayer.rotationYawHead = event.getYaw();
                    mc.thePlayer.renderYawOffset = event.getYaw();
                    mc.thePlayer.rotationPitchHead = event.getPitch();
                }
            }
        } else {
            lastYaw = event.getYaw();
            lastPitch = event.getPitch();
            if (mc.thePlayer.posY >= mc.thePlayer.prevPosY) {
                ka.hit = true;
            }
        }
    }

    private void defaultCrit(EventPreUpdate event) {
        KillAura ka = getParent();
        Criticals crit = Modules.CRITICALS;
        Speed speed = Modules.SPEED;
        Fly fly = Modules.FLY;
        AutoPot autoPot = Modules.AUTO_POT;
        boolean speedCheck = speed.isEnabled() && PlayerUtils.isMoving2();
        boolean flyCheck = fly.isEnabled() && fly.mode.getValue() instanceof LongJump;
        boolean jumpPot = autoPot.isEnabled() && autoPot.mode.getValue() instanceof JumpPot && autoPot.potting;
        if (crit.isEnabled() && !flyCheck) {
            if (crit.mode.getValue() instanceof Packets) {
                speedChecks(ka, speed, speedCheck);
                if (!ka.hit) {
                    if (!jumpPot && !speedCheck && mc.thePlayer.isCollidedVertically && !mc.gameSettings.keyBindJump.pressed && !(Modules.PHASE.isEnabled() && mc.gameSettings.keyBindSneak.pressed)) {
                        event.setY(event.getY()+crit.critY.getValue());
                        mc.thePlayer.jumpTicks = Math.max(mc.thePlayer.jumpTicks, 2);
                    }
                    event.setOnGround(true);
                    crittedForSure = false;
                    glideChecks(ka, fly, event, 0);
                } else {
                    if (mc.thePlayer.onGround) {
                        event.setY(event.getY() + 1E-13);
                    }

                    boolean adjustment = Pinger.ping() <= 60 || ka.curTar.hurtResistantTime != 11;
                    event.setOnGround(crit.moreDura.getValue() && ka.curTar.hurtResistantTime <= 14 && ka.curTar.hurtResistantTime != 10 && adjustment);//can be 15
                    crittedForSure = true;
                    glideChecks(ka, fly, event, 1);
                }
                glideChecks(ka, fly, event, 2);
            }
        }

        if (!ka.hit && (!speedCheck || !speed.timer.getValue())) {
            int pingToTicks = (int) (Pinger.ping() / 50 + 2);
            boolean flag = ka.curTar.hurtResistantTime > 10 && ka.curTar.hurtResistantTime <= 10 + pingToTicks;
            if (flag) {
                hit(ka.curTar,false);
            }
        }
    }

    public void onPost(EventPostUpdate event) {
        KillAura ka = getParent();
        if (!ka.targets.isEmpty() && ka.canReach()) {
            if (!isSpeedActive(true)) {
                if (ka.hit) {
                    hit(ka.curTar, true);
                }
            }
        }
    }

    @Override
    public void onReceive(EventPacketReceive event) {
        super.onReceive(event);
    }

    @Override
    public void onSend(EventPacketSend event) {
        super.onSend(event);
    }

    @Override
    public void onFrame(EventFrame event) {
        long currentTime = System.currentTimeMillis();
        aps.removeIf(l -> currentTime-l >= 1000);
        KillAura ka = getParent();
        if (mc.thePlayer != null && !ka.targets.isEmpty() && ka.canReach()) {
            if (isSpeedActive(true)) {
                if (event.isOnTick()) {
                    if (ka.hit) {
                        hit(ka.curTar, true);
                    }
                    ka.hit ^= true;
                }
            } else if (isSpeedActive(false)) {
                long diff = currentTime-ka.curTar.lastGenericDamage;
                float hurtTimeInMs = 50/mc.timer.timerSpeed*10-Pinger.ping();
                if (diff >= hurtTimeInMs && crittedForSure) {
                    if (attackTicks == 1 && aps.size() <= 14){
                        hit(ka.curTar, true);
                        attackTicks = 0;
                    }
                } else {
                    attackTicks = 1;
                }
            }
        }
    }

    private boolean isSpeedActive(boolean onlyOldNCP) {
        Speed speed = Modules.SPEED;
        boolean speedCheck = speed.isEnabled() && PlayerUtils.isMoving2();
        boolean isOldNCP = speedCheck && (!onlyOldNCP || speed.mode.getValue() instanceof OldNCP);
        return isOldNCP && speed.timer.getValue();
    }

    public void unSprint() {
        PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
    }
    public void reSprint() {
        PlayerUtils.packet(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
    }
    private void hit(EntityLivingBase curTar, boolean main) {
        KillAura ka = getParent();
        Speed speed = Modules.SPEED;
        boolean block = (mc.thePlayer.isBlocking() && ka.canBlock());
        boolean speedCheck = speed.isEnabled() && PlayerUtils.isMoving2();
        boolean isOldNCP = speed.mode.getValue() instanceof OldNCP && speedCheck;
        boolean needToUnSprint = curTar.hurtResistantTime <= 13 && speedCheck && (speed.mode.getValue() instanceof OldNCP || speed.mode.getValue() instanceof Fantasy && speed.korkunc.getValue());
        if (needToUnSprint) {
            unSprint();
        }
        if (ka.moreKb.getValue()) {
            reSprint();
        }
        if (isOldNCP && main) {
            if (curTar.hurtResistantTime <= 13 && (mc.thePlayer.getItemInUse() == null || mc.thePlayer.getItemInUse().getItemUseAction() != EnumAction.DRINK)) {
                PlayerUtils.sendOffset(0,0,0,false);
            }
        }
        if (block) {
            ka.unBlock();
        }

        mc.thePlayer.swingItem();
        if (!ka.improbable.getValue() || curTar.hurtResistantTime <= 13) {
            PlayerUtils.packet(new C02PacketUseEntity(curTar, C02PacketUseEntity.Action.ATTACK));
        }
        aps.add(System.currentTimeMillis());

        if (ka.improbable.getValue() && main && aps.size() <= 13 && curTar.hurtResistantTime <= 13) {
            //PlayerUtils.debug("improbable: "+aps.size()+" : "+curTar.hurtResistantTime);
            mc.thePlayer.swingItem();
            PlayerUtils.packet(new C02PacketUseEntity(curTar, C02PacketUseEntity.Action.ATTACK));
            aps.add(System.currentTimeMillis());
        }

        if (block) {
            ka.reBlock();
        }
        if (ka.critCrack.getValue() && Modules.CRITICALS.isEnabled()) {
            mc.thePlayer.onCriticalHit(curTar);
            if (PlayerUtils.getEnchantLevel(mc.thePlayer.getHeldItem(), Enchantment.sharpness) > 0) {
                mc.thePlayer.onEnchantmentCritical(curTar);
            }
        }
    }

    private void speedChecks(KillAura ka, Speed speed, boolean speedCheck) {
        if (speedCheck) {
            ka.hit = switch (speed.mode.getValue()) {
                case Fantasy _ -> speed.ticks % 2 != 0 || speed.ticks == 4;
                case NCPOnGround _ -> mc.thePlayer.onGround ? speed.ticks % 2 != 0 : ka.hit;
                default -> ka.hit;
            };
        }
    }

    private void glideChecks(KillAura ka, Fly fly, EventPreUpdate event, int stage) {
        if (!fly.isEnabled()) {
            return;
        }
        boolean isGlide = fly.isEnabled() && fly.mode.getValue() instanceof OldNCPGlide;
        Fly.GlideMode glideMode = fly.glideMode.getValue();
        boolean isCrit = glideMode == Fly.GlideMode.Crit;
        boolean isNonCrit = glideMode == Fly.GlideMode.NonCrit;
        boolean isManual = glideMode == Fly.GlideMode.Manual;
        switch (stage) {
            case 0 -> {
                if (isGlide && !isManual) {
                    OldNCPGlide glide = (OldNCPGlide) fly.mode.getValue();
                    glide.flyLimit--;
                    event.setOnGround(true);
                }
            }
            case 1 -> {
                if (!isManual && isGlide && mc.thePlayer.posY > ka.curTar.posY && ka.curTar.hurtResistantTime <= 12) {
                    OldNCPGlide glide = (OldNCPGlide) fly.mode.getValue();
                    if (glide.flyLimit > 2) {
                        if (isCrit && !glide.suicideOperation) {
                            PlayerUtils.debug("Suicide Operation: Elinde sonunda ölcen (su bulursan atla)");
                            glide.suicideOperation = true;
                        }
                        double yOff = Math.min(mc.thePlayer.posY - ka.curTar.posY, 9.5);
                        AxisAlignedBB bb = PlayerUtils.getCollision(0, -yOff, 0);
                        double prevY = event.getY();
                        if (bb != null) {
                            event.setY(bb.minY + 0.5);
                        } else {
                            event.setY(event.getY() - yOff + 0.5);
                        }
                        AxisAlignedBB blockBB = mc.thePlayer.getEntityBoundingBox().offset(0, event.getY() - prevY, 0).expand(0.0625, 0.0625, 0.0625).addCoord(0.0D, -0.55D, 0.0D);
                        if (mc.theWorld.checkBlockCollision(blockBB)) {
                            glide.resetLimit();
                        }
                    }
                    if (isNonCrit) {
                        event.setOnGround(true);
                    }
                }
            }
        }
    }
    public void onDisable() {
        KillAura ka = getParent();
        if (mc.thePlayer.isBlocking() && ka.autoBlock.getValue()) {
            mc.playerController.onStoppedUsingItem(mc.thePlayer);
        }
        ka.hit = true;
    }
    public void onEnable() {
        KillAura ka = getParent();
        ka.hit = true;
    }
}
