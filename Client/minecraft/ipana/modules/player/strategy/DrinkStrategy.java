package ipana.modules.player.strategy;

import ipana.managements.module.Modules;
import ipana.modules.combat.KillAura;
import ipana.modules.player.AutoDrink;
import ipana.utils.math.Pair;
import ipana.utils.net.Pinger;
import ipana.utils.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;

public class DrinkStrategy {
    private Minecraft mc = Minecraft.getMinecraft();

    public boolean shouldDrink(double health, double drinkDuration, boolean hasPotions) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
        KillAura ka = Modules.KILL_AURA;
        AutoDrink autoDrink = Modules.AUTO_DRINK;
        if (!ka.isEnabled() || ka.targets.isEmpty() || ka.curTar == null || !ka.canReach()) {
            return health <= player.getMaxHealth() / 2 + 4;
        }
        long targetLeftHurtTime = System.currentTimeMillis()-ka.curTar.lastGenericDamage;
        long selfLeftHurtTime = (System.currentTimeMillis()-player.lastGenericDamage);
        int hitCount = calcHitCount(player, drinkDuration);
        boolean safeDiff = health-ka.curTar.getHealth() >= 7;
        int maxTicks = 15;
        if (autoDrink.moveStrat.getValue() && health <= autoDrink.health.getValue() && autoDrink.cooldown <= 0 && autoDrink.moveStrategy.runTicks <= 0 && hasPotions && !safeDiff) {
            autoDrink.moveStrategy.runTicks = maxTicks;
            autoDrink.moveStrategy.blinkTicks = 9;
            autoDrink.lastBlinkX = mc.thePlayer.posX;
            autoDrink.lastBlinkY = mc.thePlayer.posY;
            autoDrink.lastBlinkZ = mc.thePlayer.posZ;
        }
        boolean condition1 = health < 7;
        boolean condition2 = health <= 14 && (targetLeftHurtTime <= 100 || targetLeftHurtTime > 500);
        boolean condition3 = health - ka.curTar.getHealth() >= 10;
        if (condition2 && !condition3) {
            return autoDrink.moveStrategy.runTicks <= maxTicks-5;
        }
        /*boolean shouldDrink = health <= player.getMaxHealth() / 2f + 2;
        if (attackCooldown() >= 400 && targetLeftHurtTime >= 350) {
            shouldDrink = false;
        }
        if (hitCount > 1) {
            shouldDrink = false;
        }
        if (health-ka.curTar.getHealth() >= 4) {
            shouldDrink = false;
        }
        if (ka.targets.size() == 1 && health-ka.curTar.getHealth() >= 3) {
            shouldDrink = false;
        }
        if (shouldDrink && !autoDrink.swap) {
            PlayerUtils.debug("hitCount: "+hitCount+" cooldown: "+attackCooldown()+" ht: "+targetLeftHurtTime);
        }
        return shouldDrink;*/
        return false;
    }

    private boolean hasSword(EntityLivingBase entity) {
        return entity.getHeldItem() != null && entity.getHeldItem().getItem() instanceof ItemSword;
    }

    public boolean shouldDrinkAgain(double health) {
        int hitCount = calcHitCount(mc.thePlayer, Modules.QUICK_USE.duration.getValue());
        //6 = 3 HEARTS we will get the max damage we can take
        double healthDecrease = hitCount*6;
        double healthDiff = health-healthDecrease;
        //considirng we are using instant health 2
        double potIncrease = 8;
        double healthIncrease = healthDiff + potIncrease;

        return healthDiff <= 3;
    }

    private boolean isAttackInCooldown() {
        return this.attackCooldown() < 500;
    }

    private long attackCooldown() {
        return System.currentTimeMillis()-mc.playerController.lastC09Time;
    }

    private int calcHitCount(EntityPlayerSP player, double drinkDuration) {
        long leftHurtTime = System.currentTimeMillis()-player.lastGenericDamage;
        long drinkTime = (long) (drinkDuration*1000L);
        int hitCount = 0;
        if (leftHurtTime >= 500) {
            hitCount++;
            leftHurtTime = 500;
        }
        long hurtReset = drinkTime - leftHurtTime;
        hitCount += hurtReset / 500 + 1;
        return hitCount;
    }

    private Pair<Double, Integer> simulateHHT(double damage, double health, int hurtTime, long ms) {
        int msInTicks = (int) (ms/50);
        int hitsTaken = 0;
        for (int i = 0; i < msInTicks; i++) {
            if (hurtTime <= 10) {
                health -= damage;
                hitsTaken++;
            }
            hurtTime--;
            if (hurtTime < 10) {
                hurtTime = 20;
            }
        }
        return new Pair<>(health, hitsTaken);
    }

    private int fix(int ticks) {
        if (ticks <= -10) {
            return 10;
        } else if (ticks < 0) {
            return ticks+10;
        } else {
            return ticks;
        }
    }
}
