/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.utils;

import baritone.Baritone;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.Potion;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A cached list of the best tools on the hotbar for any block
 *
 * @author Avery, Brady, leijurv
 */
public class ToolSet {

    /**
     * A cache mapping a {@link Block} to how long it will take to break
     * with this toolset, given the optimum tool is used.
     */
    private final Map<Block, Double> breakStrengthCache;

    /**
     * My buddy leijurv owned me so we have this to not create a new lambda instance.
     */
    private final Function<Block, Double> backendCalculation;

    private final EntityPlayerSP player;

    public ToolSet(EntityPlayerSP player) {
        breakStrengthCache = new HashMap<>();
        this.player = player;

        if (Baritone.settings().considerPotionEffects.value) {
            double amplifier = potionAmplifier();
            Function<Double, Double> amplify = x -> amplifier * x;
            backendCalculation = amplify.compose(this::getBestDestructionTime);
        } else {
            backendCalculation = this::getBestDestructionTime;
        }
    }

    /**
     * Using the best tool on the hotbar, how fast we can mine this block
     *
     * @param state the blockstate to be mined
     * @return the speed of how fast we'll mine it. 1/(time in ticks)
     */
    public double getStrVsBlock(IBlockState state) {
        return breakStrengthCache.computeIfAbsent(state.getBlock(), backendCalculation);
    }

    /**
     * Evaluate the material cost of a possible tool. The priority matches the
     * harvest level order; there is a chance for multiple at the same with modded tools
     * but in that case we don't really care.
     *
     * @param itemStack a possibly empty ItemStack
     * @return values from 0 up
     */
    private int getMaterialCost(ItemStack itemStack) {
        if (itemStack != null && itemStack.getItem() instanceof ItemTool tool) {
            return tool.getHarvestLevel();
        } else {
            return -1;
        }
    }

    public boolean hasSilkTouch(ItemStack stack) {
        return EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, stack) > 0;
    }

    /**
     * Calculate which tool on the hotbar is best for mining, depending on an override setting,
     * related to auto tool movement cost, it will either return current selected slot, or the best slot.
     *
     * @param b the blockstate to be mined
     * @return An int containing the index in the tools array that worked best
     */

    public int getBestSlot(Block b, boolean preferSilkTouch) {
        return getBestSlot(b, preferSilkTouch, false);
    }

    public int getBestSlot(Block b, boolean preferSilkTouch, boolean pathingCalculation) {

        /*
        If we actually want know what efficiency our held item has instead of the best one
        possible, this lets us make pathing depend on the actual tool to be used (if auto tool is disabled)
        */
        if (!Baritone.settings().autoTool.value && pathingCalculation) {
            return player.inventory.currentItem;
        }

        int best = 0;
        double highestSpeed = Double.NEGATIVE_INFINITY;
        int lowestCost = Integer.MIN_VALUE;
        boolean bestSilkTouch = false;
        IBlockState blockState = b.getDefaultState();
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = player.inventory.getStackInSlot(i);
            if (!Baritone.settings().useSwordToMine.value && itemStack.getItem() instanceof ItemSword) {
                continue;
            }
          
            if (Baritone.settings().itemSaver.value && (itemStack.getItemDamage() + Baritone.settings().itemSaverThreshold.value) >= itemStack.getMaxDamage() && itemStack.getMaxDamage() > 1) {
                continue;
            }
            double speed = calculateSpeedVsBlock(itemStack, blockState);
            boolean silkTouch = hasSilkTouch(itemStack);
            if (speed > highestSpeed) {
                highestSpeed = speed;
                best = i;
                lowestCost = getMaterialCost(itemStack);
                bestSilkTouch = silkTouch;
            } else if (speed == highestSpeed) {
                int cost = getMaterialCost(itemStack);
                if ((cost < lowestCost && (silkTouch || !bestSilkTouch)) ||
                        (preferSilkTouch && !bestSilkTouch && silkTouch)) {
                    highestSpeed = speed;
                    best = i;
                    lowestCost = cost;
                    bestSilkTouch = silkTouch;
                }
            }
        }
        return best;
    }

    /**
     * Calculate how effectively a block can be destroyed
     *
     * @param b the blockstate to be mined
     * @return A double containing the destruction ticks with the best tool
     */
    private double getBestDestructionTime(Block b) {
        ItemStack stack = player.inventory.getStackInSlot(getBestSlot(b, false, true));
        return calculateSpeedVsBlock(stack, b.getDefaultState()) * avoidanceMultiplier(b);
    }

    private double avoidanceMultiplier(Block b) {
        return Baritone.settings().blocksToAvoidBreaking.value.contains(b) ? Baritone.settings().avoidBreakingMultiplier.value : 1;
    }

    /**
     * Calculates how long would it take to mine the specified block given the best tool
     * in this toolset is used. A negative value is returned if the specified block is unbreakable.
     *
     * @param item  the item to mine it with
     * @param state the blockstate to be mined
     * @return how long it would take in ticks
     */
    public static double calculateSpeedVsBlock(ItemStack item, IBlockState state) {
        float hardness = state.getBlock().getBlockHardness();
        if (hardness < 0) {
            return -1;
        }
        if (item == null) {
            return -1;
        }
        //TODO: item.getDestroySpeed(state);
        float speed = item.getStrVsBlock(state.getBlock());
        if (speed > 1) {
            int effLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, item);
            if (effLevel > 0) {
                speed += effLevel * effLevel + 1;
            }
        }

        speed /= hardness;
        if (state.getBlock().getMaterial().isToolNotRequired() || item.canHarvestBlock(state.getBlock())) {
            return speed / 30;
        } else {
            return speed / 100;
        }
    }

    /**
     * Calculates any modifier to breaking time based on status effects.
     *
     * @return a double to scale block breaking speed.
     */
    private double potionAmplifier() {
        double speed = 1;
        if (player.isPotionActive(Potion.digSpeed)) {
            speed *= 1 + (player.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2;
        }
        if (player.isPotionActive(Potion.digSlowdown)) {
            switch (player.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
                case 0 -> speed *= 0.3;
                case 1 -> speed *= 0.09;
                case 2 -> speed *= 0.0027; // you might think that 0.09*0.3 = 0.027 so that should be next, that would make too much sense. it's 0.0027.
                default -> speed *= 0.00081;
            }
        }
        return speed;
    }
}
