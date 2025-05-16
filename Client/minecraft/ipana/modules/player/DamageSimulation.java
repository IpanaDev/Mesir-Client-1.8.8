package ipana.modules.player;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;

public class DamageSimulation {

    public float simulateDeath(EntityPlayer damager, EntityPlayer damaged) {
        if (damager == null || damaged == null) {
            return 0;
        }
        float percentage = 0.1f;

        float f = 1f;
        float enchantLevel = 1.25f * EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, damager.getHeldItem());
        if (damager.getHeldItem() != null) {
            Item item = damager.getHeldItem().getItem();
            if (item instanceof ItemSword sword) {
                f += sword.attackDamage;
            } else if (item instanceof ItemTool tool) {
                f += tool.damageVsEntity;
            }
        }
        for (PotionEffect potion : damager.getActivePotionEffects()) {
            if (potion.getPotionID() == Potion.damageBoost.id) {
                f += f * 1.3D * (potion.getAmplifier() + 1);
            } else if (potion.getPotionID() == Potion.weakness.id) {
                f += f * -0.5F * (potion.getAmplifier() + 1);
            }
        }

        if (f > 0 || enchantLevel > 0) {
            boolean canCrit = !damager.isOnLadder() && !damager.isInWater() && !damager.isPotionActive(Potion.blindness) && damager.ridingEntity == null;
            if (f > 0 && canCrit) {
                f *= 1.5f;
            }
            f += enchantLevel;

            float healthLeft = Math.max(0, applyDamage(damaged, DamageSource.causePlayerDamage(damager), f));
            float maxValue = 100f / damaged.getMaxHealth();
            int predict = 2;
            int hurtTime = Math.max(1, damaged.hurtResistantTime - (10 + predict));
            percentage = (damaged.getMaxHealth() - healthLeft) * maxValue / hurtTime;
            //PlayerUtils.debug(percentage+ " : "+ healthLeft +" : "+hurtTime);
        }
        return percentage;
    }

    private float applyDamage(EntityPlayer damaged, DamageSource damageSrc, float damageAmount) {
        float health = damaged.getHealth();
        if (!damageSrc.isUnblockable() && damaged.isBlocking() && damageAmount > 0.0F) {
            damageAmount = (1.0F + damageAmount) * 0.5F;
        }

        damageAmount = this.applyArmorCalculations(damaged, damageSrc, damageAmount);
        damageAmount = this.applyPotionDamageCalculations(damaged, damageSrc, damageAmount);
        damageAmount = Math.max(damageAmount - damaged.getAbsorptionAmount(), 0.0F);

        if (damageAmount != 0.0F) {
            health -= damageAmount;
        }
        return health;
    }

    protected float applyArmorCalculations(EntityPlayer damaged, DamageSource source, float damage) {
        if (!source.isUnblockable()) {
            int i = 25 - damaged.getTotalArmorValue();
            float f = damage * (float)i;
            damage = f / 25.0F;
        }
        return damage;
    }

    protected float applyPotionDamageCalculations(EntityPlayer damaged, DamageSource source, float damage) {
        if (source.isDamageAbsolute()) {
            return damage;
        } else {
            if (damaged.isPotionActive(Potion.resistance) && source != DamageSource.outOfWorld) {
                int i = (damaged.getActivePotionEffect(Potion.resistance).getAmplifier() + 1) * 5;
                int j = 25 - i;
                float f = damage * (float) j;
                damage = f / 25.0F;
            }

            if (damage <= 0.0F) {
                return 0.0F;
            } else {
                int k = EnchantmentHelper.getEnchantmentModifierDamage(damaged.getInventory(), source);

                if (k > 20) {
                    k = 20;
                }

                if (k > 0) {
                    int l = 25 - k;
                    float f1 = damage * (float) l;
                    damage = f1 / 25.0F;
                }

                return damage;
            }
        }
    }
}
