package ipana.utils.player;

import com.google.common.base.Predicates;
import ipana.events.EventMoving;
import ipana.managements.module.Modules;
import ipana.modules.movement.Fly;
import ipana.modules.movement.modes.fly.OldNCPGlide;
import ipana.utils.StringUtil;
import ipana.utils.baritone.BaritoneHelper;
import ipana.utils.math.MathUtils;
import ipana.utils.ncp.listener.NCPListener;
import ipana.utils.ncp.utilities.FrictionAxisVelocity;
import ipana.utils.ncp.utilities.SimpleEntry;
import ipana.utils.ncp.utilities.VelocityData;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtils {
    private static Minecraft mc = Minecraft.getMinecraft();
    private static NCPListener ncpListener = new NCPListener();
    public final static ArrayList<Flag> predictedFlags = new ArrayList<>();

    public static void packet(Packet<? extends INetHandler> packet) {
        if (mc.thePlayer != null && mc.thePlayer.sendQueue != null) {
            mc.thePlayer.sendQueue.addToSendQueue(packet);
        }
    }
    public static ItemStack[] getInventory(EntityLivingBase ent) {
        return ent.getInventory();
    }

    public static ItemStack getArmor(EntityLivingBase e,Armor armor) {
        ItemStack zaa = null;
        int armorId = Armor.VALUES.length-armor.ordinal()-1;
        if (getInventory(e)[armorId] != null) {
            zaa = getInventory(e)[armorId];
        }
        return zaa;
    }
    public static int getArmorDura(EntityLivingBase e,Armor armor) {
        int diff = 0;
        if (getArmor(e,armor) != null) {
            diff = getArmor(e,armor).getMaxDamage()-getArmor(e,armor).getItemDamage();
        }
        return diff;
    }
    public static void swapBegin() {
        var ms = System.currentTimeMillis();
        var slot = 36 + mc.thePlayer.inventory.currentItem;
        swapBegin(slot);
        ncpListener().ignoredSlots.push(new NCPListener.SlotProperty(-1, -1, false, ms));
        ncpListener().ignoredSlots.push(new NCPListener.SlotProperty(0, slot, true, ms));
        ncpListener().ignoredWindows.push(new NCPListener.WindowProperty(mc.thePlayer.inventory.getItemStack(), slot, ms));
        ncpListener().ignoredTransactions.push(new NCPListener.TransactionProperty(mc.thePlayer.openContainer.getTransactionID(), ms));
        //ncpListener().ignoredSlots.add(new NCPListener.SlotProperty(0,0, true, ms));
        //ncpListener().ignoredSlots.add(new NCPListener.SlotProperty(0,36, true, ms));
    }
    public static void swapEnd() {
        swapEnd(36 + mc.thePlayer.inventory.currentItem);
    }
    public static void swapBegin(int slot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 0, 0, mc.thePlayer);
        PlayerUtils.packet(new C0FPacketConfirmTransaction(0, mc.thePlayer.openContainer.getTransactionID(), true));
    }
    public static void swapEnd(int slot) {
        int drag = -999;
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, drag, 0, 5, mc.thePlayer);
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 1, 5, mc.thePlayer);
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, drag, 2, 5, mc.thePlayer);
    }
    public static void swapItem(int slot, int hotbarNum) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hotbarNum, 2, mc.thePlayer);
    }
    public static void swapItem(int slot, int hotbarNum,int mode) {
        if (mode == 2173) {
            short short1 = mc.thePlayer.openContainer.getNextTransactionID(mc.thePlayer.inventory);
            PlayerUtils.packet(new C0EPacketClickWindow(mc.thePlayer.inventoryContainer.windowId, slot, hotbarNum, 2, mc.thePlayer.getHeldItem(), short1));
        } else if (mode == 502) {
            mc.thePlayer.openContainer.slotClick(slot, hotbarNum, 2, mc.thePlayer);
        } else {
            short short1 = mc.thePlayer.openContainer.getNextTransactionID(mc.thePlayer.inventory);
            ItemStack itemstack = mc.thePlayer.openContainer.slotClick(slot, hotbarNum, mode, mc.thePlayer);
            PlayerUtils.packet(new C0EPacketClickWindow(mc.thePlayer.inventoryContainer.windowId, slot, hotbarNum, mode, itemstack, short1));
        }
    }

    public static void fixInventory() {
        short short1 = mc.thePlayer.openContainer.getNextTransactionID(mc.thePlayer.inventory);
        PlayerUtils.packet(new C0EPacketClickWindow(mc.thePlayer.inventoryContainer.windowId, -1, 0, 0, new ItemStack(Item.getItemFromBlock(Blocks.bedrock)), short1));
    }
    public static AxisAlignedBB getBBFromXYZ(double x, double y, double z) {
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = mc.theWorld.getBlockState(pos);
        Block block = state.getBlock();
        return block.getCollisionBoundingBox(mc.theWorld, pos, state);
    }
    public static boolean isInBox(AxisAlignedBB bb) {
        if (bb == null) {
            return false;
        }
        return mc.thePlayer.getEntityBoundingBox().intersectsWith(bb);
    }
    public static void cancelFlag(double x, double y, double z, boolean setPos) {
        cancelFlag(x, y, z, true, setPos);
    }
    public static void cancelFlag(double x, double y, double z, boolean sendPacket, boolean setPos) {
        predictedFlags.add(new Flag(x, y, z, System.currentTimeMillis()));
        if (sendPacket) {
            send(x, y, z);
        }
        if (setPos) {
            mc.thePlayer.setPosition(x, y, z);
        }
        ncpListener().sfHorizontalBuffer = 0.0;
        ncpListener().data().clearHorVel();
    }
    public static boolean containsFlag(double x, double y, double z) {
        for (Flag flag : predictedFlags) {
            double offX = MathUtils.fixFormat(flag.x, 5);
            double offY = MathUtils.fixFormat(flag.y, 5);
            double offZ = MathUtils.fixFormat(flag.z, 5);
            double flagX = MathUtils.fixFormat(x, 5);
            double flagY = MathUtils.fixFormat(y, 5);
            double flagZ = MathUtils.fixFormat(z, 5);
            return offX == flagX && offY == flagY && offZ == flagZ;
        }
        return false;
    }
    public static double[] calculate(double speed) {
        return calculate(speed, BaritoneHelper.lastBaritoneYaw());
    }
    public static double[] calculate(double speed,float neededYaw) {
        return calculate(speed, neededYaw, 45);
    }
    public static double[] calculate(double speed,float neededYaw, float strafeYaw) {
        double forward = mc.thePlayer.movementInput.moveForward;
        double strafe = mc.thePlayer.movementInput.moveStrafe;
        float yaw = neededYaw;
        if (forward != 0.0D) {
            if (strafe > 0.0D) {
                yaw += (forward > 0.0D ? -strafeYaw : strafeYaw);
            } else if (strafe < 0.0D) {
                yaw += (forward > 0.0D ? strafeYaw : -strafeYaw);
            }
            strafe = 0.0D;
            if (forward > 0.0D) {
                forward = 1.0D;
            } else if (forward < 0.0D) {
                forward = -1.0D;
            }
        } else {
            if (strafe > 0) {
                strafe = 1;
            } else if (strafe < 0) {
                strafe = -1;
            }
        }
        double radian = Math.toRadians(yaw + 90.0F);
        double cos = Math.cos(radian);
        double sin = Math.sin(radian);
        double xSpeed = forward * speed * cos + strafe * speed * sin;
        double zSpeed = forward * speed * sin - strafe * speed * cos;
        return new double[]{xSpeed,zSpeed};
    }
    public static double[] calculate2(double speed,float neededYaw, float forward) {
        //TODO: i deleted strafe thing from here i hope no problems in future
        double xSpeed = forward * speed * Math.cos(Math.toRadians(neededYaw + 90.0F));
        double zSpeed = forward * speed * Math.sin(Math.toRadians(neededYaw + 90.0F));
        return new double[]{xSpeed,zSpeed};
    }
    public static boolean isInBlock() {
        for (int x = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minX); x < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxX) + 1; x++) {
            for (int y = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY); y < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxY) + 1; y++) {
                for (int z = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxZ) + 1; z++) {
                    Block block = mc.theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
                    if (block != null) {
                        AxisAlignedBB boundingBox = block.getCollisionBoundingBox(mc.theWorld, new BlockPos(x, y, z), mc.theWorld.getBlockState(new BlockPos(x, y, z)));
                        if ((boundingBox != null) && (mc.thePlayer.getEntityBoundingBox().intersectsWith(boundingBox))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    public static boolean isInBlock(double dist) {
        for (double x = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minX); x < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxX) + 1; x+=1) {
            for (double y = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minY); y < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxY) + 1; y++) {
                for (double z = MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().minZ); z < MathHelper.floor_double(mc.thePlayer.getEntityBoundingBox().maxZ) + 1; z+=1) {
                    AxisAlignedBB boundingBox = new AxisAlignedBB(x+dist,y,z+dist,x+1-dist,y,z+1-dist);
                    if (!(mc.theWorld.getBlockState(new BlockPos(x,y,z)).getBlock() instanceof BlockAir)) {
                        if (mc.thePlayer.getEntityBoundingBox().intersectsWith(boundingBox)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    public static List<EntityLivingBase> getLivingList() {
        return mc.theWorld.livingEntities;
    }
    public static List<EntityPlayer> getPlayers() {
        return mc.theWorld.playerEntities;
    }

    public static EntityPlayer getPlayer(String name) {
        for (EntityPlayer player : getPlayers()) {
            if (player.getName().equals(name)) {
                return player;
            }
        }
        return null;
    }

    public static boolean isMoving() {
        return (mc.gameSettings.keyBindForward.pressed || mc.gameSettings.keyBindBack.pressed || mc.gameSettings.keyBindLeft.pressed || mc.gameSettings.keyBindRight.pressed);
    }

    public static boolean isMoving2() {
        return mc.thePlayer != null && (mc.thePlayer.moveForward != 0 || mc.thePlayer.moveStrafing != 0);
    }
    public static boolean wasMoving() {
        return (mc.thePlayer.prevForward != 0 || mc.thePlayer.prevStrafe != 0);
    }

    public static void setSpeed(double speed) {
        mc.thePlayer.motionX = (-MathHelper.sin(mc.thePlayer.getDirection()) * speed);
        mc.thePlayer.motionZ = (MathHelper.cos(mc.thePlayer.getDirection()) * speed);
    }
    public static double getBaseMoveSpeed() {
        if (mc.thePlayer == null) {
            return 0;
        }
        double baseSpeed = 0.2873;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= (1.0D + 0.2D * (amplifier + 1));
        }
        if (mc.thePlayer.isPotionActive(Potion.moveSlowdown)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSlowdown).getAmplifier();
            baseSpeed *= (1.0D + -0.15D * (amplifier + 1));
        }
        if (mc.thePlayer.onIce) {
            baseSpeed *= 2.5;
        }
        return baseSpeed;
    }
    public static double getBaseMoveSpeed(double moveSpeed, boolean calculate) {
        double baseSpeed = moveSpeed;
        if (mc.thePlayer.isInWater()) {
            baseSpeed = 0.221 * 0.5203619909502263D;
        }
        if (calculate) {
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
                baseSpeed *= (1.0D + 0.2D * (amplifier + 1));
            }
        }
        if (mc.thePlayer.onIce) {
            baseSpeed *= 2.5;
        }
        return baseSpeed;
    }

    public static double baseSpeed311(double xDist, double zDist) {
        double baseSpeed = 0.221;
        if (mc.thePlayer.isInWeb) {
            baseSpeed *= 0.4751131221719457D;
        } else if (isInLiquid()) {
            baseSpeed *= 0.5203619909502263D;
        } else if (mc.thePlayer.isSneaking()) {
            baseSpeed *= 0.5882352941176471D;
        } else {
            baseSpeed *= 1.3122171945701357;
        }
        //blocking == 0.7239819004524887

        if (isInLiquid() && downStream(xDist, zDist)) {
            baseSpeed *= 1.6521739130434783D;
        }

        if (mc.thePlayer.onIce) {
            baseSpeed *= 2.5;
        }
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= (1.0D + 0.2D * (amplifier + 1));
        }

        return baseSpeed;
    }
    public static double baseSpeed313(double xDist, double zDist) {
        double baseSpeed = 0.221;
        if (mc.thePlayer.isInWeb) {
            baseSpeed *= 0.4751131221719457D;
        } else if (isInLiquid()) {
            baseSpeed *= 0.5203619909502263D;
        } else if (mc.thePlayer.isSneaking()) {
            baseSpeed *= 0.5882352941176471D;
        } else if (mc.thePlayer.isSprinting()) {
            baseSpeed *= 1.3;
        }

        if (isInLiquid() && downStream(xDist, zDist)) {
            baseSpeed *= 1.6521739130434783D;
        }

        if (mc.thePlayer.onIce) {
            baseSpeed *= 2.5;
        }
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= (1.0D + 0.2D * (amplifier + 1));
        }

        return baseSpeed;
    }
    public static boolean downStream(double xDist, double zDist) {
        if (xDist > 0) {
            xDist = 1;
        } else if (xDist < 0) {
            xDist = -1;
        }
        if (zDist > 0) {
            zDist = 1;
        } else if (zDist < 0) {
            zDist = -1;
        }
        IBlockState underBlock = mc.theWorld.getBlockState(mc.thePlayer.getPosition2());
        IBlockState offsetUnderBlock = mc.theWorld.getBlockState(mc.thePlayer.getPosition2().add(xDist, 0, zDist));
        if (underBlock.getBlock() instanceof BlockLiquid && offsetUnderBlock.getBlock() instanceof BlockLiquid) {
            int fromLevel = underBlock.getValue(BlockLiquid.LEVEL);
            int toLevel = offsetUnderBlock.getValue(BlockLiquid.LEVEL);
            return fromLevel < toLevel;
        } else {
            return false;
        }
    }

    public static double diffBetweenCollision(AxisAlignedBB bb) {
        double colX = (bb.minX+bb.maxX)/2;
        double colZ = (bb.minZ+bb.maxZ)/2;
        double xDiff = mc.thePlayer.posX - colX;
        double zDiff = mc.thePlayer.posZ - colZ;
        return Math.hypot(xDiff, zDiff);
    }

    public static Vec3 testForCollision(double x, double y, double z) {
        double posX = mc.thePlayer.posX;
        double posY = mc.thePlayer.posY;
        double posZ = mc.thePlayer.posZ;

        mc.thePlayer.moveEntityNoEvent(x, y, z);
        boolean expected = mc.thePlayer.posX == posX + x && mc.thePlayer.posY == posY + y && mc.thePlayer.posZ == posZ + z;
        var Vector = expected ? null : new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        mc.thePlayer.setPosition(posX, posY, posZ);
        return Vector;
    }

    public static AxisAlignedBB getCollision(double x, double y, double z) {
        return getCollision(x, y, z, false);
    }
    public static AxisAlignedBB getCollision(double x, double y, double z, boolean entityCheck) {
        AxisAlignedBB axis = mc.thePlayer.getEntityBoundingBox().offset(0,0,0);
        AxisAlignedBB wanted = axis.offset(x, y, z);
        boolean hasX = x != 0;
        boolean hasZ = z != 0;
        boolean hasY = y != 0;
        if (hasX || hasY || hasZ) {
            List<AxisAlignedBB> boxes = mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().addCoord(x, y, z), entityCheck);
            for (AxisAlignedBB bb : boxes) {
                x = bb.calculateXOffset(mc.thePlayer.getEntityBoundingBox(), x);
                y = bb.calculateYOffset(mc.thePlayer.getEntityBoundingBox(), y);
                z = bb.calculateZOffset(mc.thePlayer.getEntityBoundingBox(), z);
            }
            mc.thePlayer.setEntityBoundingBox(mc.thePlayer.getEntityBoundingBox().offset(x, y, z));
        }
        if (mc.thePlayer.getEntityBoundingBox().isEqual(wanted)) {
            mc.thePlayer.setEntityBoundingBox(axis);
            return null;
        }
        AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox();
        mc.thePlayer.setEntityBoundingBox(axis);
        return bb;
    }
    public static AxisAlignedBB getCollision2(double x, double y, double z) {
        AxisAlignedBB axis = mc.thePlayer.getEntityBoundingBox().offset(0,0,0);
        AxisAlignedBB wanted = axis.offset(x, y, z);
        boolean hasX = x != 0;
        boolean hasZ = z != 0;
        boolean hasY = y != 0;
        if (hasX || hasY || hasZ) {
            List<AxisAlignedBB> boxes = mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().addCoord(x, y, z), false);
            if (hasY) {
                for (AxisAlignedBB bb : boxes) {
                    y = bb.calculateYOffset(mc.thePlayer.getEntityBoundingBox(), y);
                }
                mc.thePlayer.setEntityBoundingBox(mc.thePlayer.getEntityBoundingBox().offset(0.0D, y, 0.0D));
            }
            if (hasX) {
                for (AxisAlignedBB bb : boxes) {
                    x = bb.calculateXOffset(mc.thePlayer.getEntityBoundingBox(), x);
                }

                mc.thePlayer.setEntityBoundingBox(mc.thePlayer.getEntityBoundingBox().offset(x, 0.0D, 0.0D));
            }
            if (hasZ) {
                for (AxisAlignedBB bb : boxes) {
                    z = bb.calculateZOffset(mc.thePlayer.getEntityBoundingBox(), z);
                }

                mc.thePlayer.setEntityBoundingBox(mc.thePlayer.getEntityBoundingBox().offset(0.0D, 0.0D, z));
            }
        }
        if (mc.thePlayer.getEntityBoundingBox().isEqual(wanted)) {
            mc.thePlayer.setEntityBoundingBox(axis);
            return null;
        }
        AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox();
        mc.thePlayer.setEntityBoundingBox(axis);
        return bb;
    }
    public static boolean isInLiquid() {
        return mc.thePlayer.isInLava() || mc.thePlayer.isInWater();
    }

    public static double getSpeed(EventMoving event) {
        return Math.sqrt(event.getX()*event.getX()+event.getZ()*event.getZ());
    }
    public static double testHFreedom(double hDistanceAboveLimit) {
        FrictionAxisVelocity hVel = ncpListener().data().horizontalVelocity().copy();
        double hFreedom = hVel.getFreedom();
        if (hFreedom < hDistanceAboveLimit) {
            hFreedom += hVel.use(hDistanceAboveLimit - hFreedom);
        }
        return hFreedom;
    }
    public static double getHFreedomNoCopy(double hDistanceAboveLimit) {
        double hFreedom = ncpListener().data().getHorizontalFreedom();
        if (hFreedom < hDistanceAboveLimit) {
            hFreedom += ncpListener().data().useHorizontalVelocity(hDistanceAboveLimit - hFreedom);
        }
        return hFreedom;
    }
    public static double getHFreedom(double maxSpeed) {
        double hDist = 10;
        double base = getBaseMoveSpeed();
        double hDistanceAboveLimit = hDist-base;
        double defaultLimit = hDistanceAboveLimit;
        double hFreedom;
        FrictionAxisVelocity oldHorizontal = ncpListener().data().horizontalVelocity().copy();
        if (hDistanceAboveLimit > 0.0) {
            hFreedom = ncpListener().data().getHorizontalFreedom();
            if (hFreedom < hDistanceAboveLimit) {
                hFreedom += ncpListener().data().useHorizontalVelocity(hDistanceAboveLimit - hFreedom);
            }
            if (hFreedom > 0.0) {
                hDistanceAboveLimit = Math.max(0.0, hDistanceAboveLimit - hFreedom);
            }
        }
        if (hDistanceAboveLimit > 0.0 && ncpListener().sfHorizontalBuffer > 0.0) {
            final double amount = Math.min(ncpListener().sfHorizontalBuffer, hDistanceAboveLimit);
            hDistanceAboveLimit -= amount;
        }
        ncpListener().data().setHorizontalVelocity(oldHorizontal);
        double limitDiff = defaultLimit-ncpListener().sfHorizontalBuffer-hDistanceAboveLimit;
        if (limitDiff > 0) {
            hDistanceAboveLimit = Math.min(limitDiff, maxSpeed);
            hFreedom = ncpListener().data().getHorizontalFreedom();
            if (hFreedom < hDistanceAboveLimit) {
                hFreedom += ncpListener().data().useHorizontalVelocity(hDistanceAboveLimit - hFreedom);
            }
            if (hFreedom > 0.0) {
                hDistanceAboveLimit = Math.max(0.0, hDistanceAboveLimit - hFreedom);
            }
            if (hDistanceAboveLimit > 0.0 && ncpListener().sfHorizontalBuffer > 0.0) {
                final double amount = Math.min(ncpListener().sfHorizontalBuffer, hDistanceAboveLimit);
                hDistanceAboveLimit -= amount;
            }
            if (hDistanceAboveLimit > 1E-13) {
                PlayerUtils.debug("malmısın olm: "+hDistanceAboveLimit);
                return 0;
            }
            return limitDiff;
        } else {
            return 0;
        }
    }

    public static double getVFreedom(double yDistance) {
        VelocityData data = PlayerUtils.ncpListener().data();
        SimpleEntry entry = data.getOrUseVerticalVelocity(yDistance);
        return entry != null ? entry.value : -2173;
    }

    public static int getMS(Entity entity) {
        if (mc.getNetHandler() == null) {
            return 0;
        }
        NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(entity.getUniqueID());
        if (info == null) {
            return 0;
        }
        return info.getResponseTime();
    }

    public static Enchantment getEnchant(ItemStack itemStack,Enchantment enchantment) {
        if (itemStack.getEnchantmentTagList() != null && itemStack.getEnchantmentTagList().tagCount() > 0) {
            for (int i = 0; i < itemStack.getEnchantmentTagList().tagCount(); i++) {
                if (itemStack.getEnchantmentTagList().getCompoundTagAt(i).getShort("id") == enchantment.effectId) {
                    return Enchantment.getEnchantmentById(itemStack.getEnchantmentTagList().getCompoundTagAt(i).getShort("id"));
                }
            }
        }
        return null;
    }
    public static ArrayList<Enchant> getEnchants(ItemStack itemStack) {
        ArrayList<Enchant> list = new ArrayList<>();
        NBTTagList enchantTags = itemStack.getEnchantmentTagList();
        if (enchantTags != null && enchantTags.tagCount() > 0) {
            for (int i = 0; i < enchantTags.tagCount(); i++) {
                NBTTagCompound tag = enchantTags.getCompoundTagAt(i);
                int id = tag.getShort("id");
                int lvl = tag.getShort("lvl");
                list.add(new Enchant(Enchantment.getEnchantmentById(id),id,lvl));
            }
        }
        return list;
    }
    public static int getEnchantLevel(ItemStack itemStack,Enchantment enchantment) {
        return EnchantmentHelper.getEnchantmentLevel(enchantment.effectId,itemStack);
    }

    public static void debug(Object str) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(StringUtil.combine(EnumChatFormatting.GOLD, "[", EnumChatFormatting.AQUA, "Mesir", EnumChatFormatting.GOLD, "] ", EnumChatFormatting.WHITE, str)));
        }
    }
    public static void debug(Object... str) {
        if (mc.thePlayer != null) {
            debug(StringUtil.toString(str, ","));
        }
    }
    public static void updatePotionEffects() {
        for (PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
            effect.onUpdate(mc.thePlayer);
            effect.deincrementDuration();
        }
    }

    public static void skipTicks() {
        skipTicks(true);
    }

    public static void skipTicks(boolean onGround) {
        Fly fly = Modules.FLY;
        if (fly.isEnabled() && fly.mode.getValue() instanceof OldNCPGlide glide) {
            glide.flyLimit--;
        }
        PlayerUtils.packet(new C03PacketPlayer(onGround));
    }

    public static void send(double x, double y, double z, boolean ground) {
        PlayerUtils.packet(new C03PacketPlayer.C04PacketPlayerPosition(x,y,z,ground));
    }
    public static void send(double x, double y, double z, float yaw, float pitch, boolean ground) {
        PlayerUtils.packet(new C03PacketPlayer.C06PacketPlayerPosLook(x,y,z,yaw,pitch,ground));
    }
    public static void send(double x, double y, double z) {
        send(x,y,z, mc.thePlayer.onGround);
    }
    public static void sendOffset(double x, double y, double z) {
        send(mc.thePlayer.posX+x,mc.thePlayer.posY+y,mc.thePlayer.posZ+z, mc.thePlayer.onGround);
    }
    public static void sendOffset(double x, double y, double z, float yaw, float pitch) {
        send(mc.thePlayer.posX+x,mc.thePlayer.posY+y,mc.thePlayer.posZ+z, mc.thePlayer.rotationYaw+yaw, mc.thePlayer.rotationPitch+pitch, mc.thePlayer.onGround);
    }
    public static void sendOffset(double x, double y, double z, boolean ground) {
        send(mc.thePlayer.posX+x,mc.thePlayer.posY+y,mc.thePlayer.posZ+z, ground);
    }

    public static void damage() {
        //Old Damage 49 * 2 + 1 = 99 Packets (Fall: 3.0625) VerticalFreedom: 0.164773281
        //New Damage 24 * 2 + 4 = 52 Packets (Fall: 3.125)  VerticalFreedom: 0.24813599859094576
        //How is this working? I have no idea
        int length = 24;
        for (int i = 0; i < length; i++) {
            sendOffset(0,0.0625,0,false);
            sendOffset(0,-0.0625,0,false);
        }
        sendOffset(0,0.0625,0,false);
        sendOffset(0,0,0,false);
        sendOffset(0,0.0625,0,false);
        sendOffset(0,0,0,true);
    }
    public static Face8Direction[] getCardinalDirection(float yaw) {
        double rotation = (yaw - 180) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 22.5) {
            return new Face8Direction[]{Face8Direction.NORTH, Face8Direction.NONE};
        } else if (22.5 <= rotation && rotation < 67.5) {
            return new Face8Direction[]{Face8Direction.NORTH, Face8Direction.EAST};
        } else if (67.5 <= rotation && rotation < 112.5) {
            return new Face8Direction[]{Face8Direction.EAST, Face8Direction.NONE};
        } else if (112.5 <= rotation && rotation < 157.5) {
            return new Face8Direction[]{Face8Direction.SOUTH, Face8Direction.EAST};
        } else if (157.5 <= rotation && rotation < 202.5) {
            return new Face8Direction[]{Face8Direction.SOUTH, Face8Direction.NONE};
        } else if (202.5 <= rotation && rotation < 247.5) {
            return new Face8Direction[]{Face8Direction.SOUTH, Face8Direction.WEST};
        } else if (247.5 <= rotation && rotation < 292.5) {
            return new Face8Direction[]{Face8Direction.WEST, Face8Direction.NONE};
        } else if (292.5 <= rotation && rotation < 337.5) {
            return new Face8Direction[]{Face8Direction.NORTH, Face8Direction.WEST};
        } else if (337.5 <= rotation && rotation < 360.0) {
            return new Face8Direction[]{Face8Direction.NORTH, Face8Direction.NONE};
        } else {
            return new Face8Direction[]{Face8Direction.NONE, Face8Direction.NONE};
        }
    }
    public static EnumFacing[] getDirection(float yaw) {
        double rotation = (yaw - 180) % 360;
        if (rotation < 0) {
            rotation += 360.0;
        }
        if (0 <= rotation && rotation < 22.5) {
            return new EnumFacing[]{EnumFacing.NORTH};
        } else if (22.5 <= rotation && rotation < 67.5) {
            return new EnumFacing[]{EnumFacing.NORTH, EnumFacing.EAST};
        } else if (67.5 <= rotation && rotation < 112.5) {
            return new EnumFacing[]{EnumFacing.EAST};
        } else if (112.5 <= rotation && rotation < 157.5) {
            return new EnumFacing[]{EnumFacing.SOUTH, EnumFacing.EAST};
        } else if (157.5 <= rotation && rotation < 202.5) {
            return new EnumFacing[]{EnumFacing.SOUTH};
        } else if (202.5 <= rotation && rotation < 247.5) {
            return new EnumFacing[]{EnumFacing.SOUTH, EnumFacing.WEST};
        } else if (247.5 <= rotation && rotation < 292.5) {
            return new EnumFacing[]{EnumFacing.WEST};
        } else if (292.5 <= rotation && rotation < 337.5) {
            return new EnumFacing[]{EnumFacing.NORTH, EnumFacing.WEST};
        } else if (337.5 <= rotation && rotation < 360.0) {
            return new EnumFacing[]{EnumFacing.NORTH};
        } else {
            return new EnumFacing[]{};
        }
    }
    public static NCPListener ncpListener() {
        return ncpListener;
    }

    public static EnumFacing getFacing(BlockPos from, BlockPos to) {
        if (from.getX() - to.getX() > 0) {
            return EnumFacing.WEST;
        } else if (from.getX() - to.getX() < 0) {
            return EnumFacing.EAST;
        } else if (from.getZ() - to.getZ() > 0) {
            return EnumFacing.NORTH;
        } else if (from.getZ() - to.getZ() < 0) {
            return EnumFacing.SOUTH;
        } else if (from.getY() - to.getY() > 0) {
            return  EnumFacing.UP;
        } else if (from.getY() - to.getY() < 0) {
            return EnumFacing.DOWN;
        }
        return null;
    }
    public static boolean rayTraceSimple(AxisAlignedBB bb) {
        int traceAmount = 1;
        double[] corners = new double[] {
                bb.minX, bb.minY, bb.minZ,
                bb.minX, bb.minY, bb.maxZ,
                bb.minX, bb.maxY, bb.minZ,
                bb.minX, bb.maxY, bb.maxZ,
                bb.maxX, bb.maxY, bb.maxZ,
                bb.maxX, bb.maxY, bb.minZ,
                bb.maxX, bb.minY, bb.minZ,
                bb.maxX, bb.minY, bb.maxZ
        };
        for (int i = 0; i < corners.length; i+=3) {
            double x = corners[i];
            double y = corners[i+1];
            double z = corners[i+2];
            double xDiff = x - mc.thePlayer.posX;
            double zDiff = z - mc.thePlayer.posZ;
            double yDiff = y - mc.thePlayer.posY - mc.thePlayer.getEyeHeight();
            double dist = MathHelper.sqrt_double(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
            double degree180 = 180.0D / Math.PI;
            double yaw = Math.toRadians((Math.atan2(zDiff, xDiff) * degree180) - 90.0F);
            double pitch = Math.toRadians(-(Math.atan2(yDiff, dist) * degree180));
            double sinX = -Math.sin(yaw);
            double sinY = Math.sin(-pitch);
            double sinZ = Math.cos(yaw);
            for(int j = 0; j < dist; j += traceAmount) {
                if (dist-j < 0.2) {
                    break;
                }
                double dX = sinX * j;
                double dY = sinY * j;
                double dZ = sinZ * j;
                BlockPos pos = new BlockPos(mc.thePlayer.posX+dX, mc.thePlayer.posY+mc.thePlayer.getEyeHeight()+dY, mc.thePlayer.posZ+dZ);
                if (mc.theWorld.getBlockState(pos).getBlock().isOpaqueCube()) {
                    return false;
                }
            }
        }
        return true;
    }

    public static MovingObjectPosition getMouseOver(float yaw, float pitch, boolean useRawPosition) {
        Entity entity = mc.getRenderViewEntity();
        if (entity != null && mc.theWorld != null && entity instanceof EntityLivingBase base) {
            double d0 = mc.playerController.getBlockReachDistance();
            float lastYaw = base.rotationYawHead;
            float lastPitch = base.rotationPitchHead;
            base.rotationYawHead = yaw;
            base.rotationPitchHead = pitch;
            Vec3[] positions = new Vec3[mc.theWorld.loadedEntityList.size()];
            if (useRawPosition) {
                for (int i = 0; i < positions.length; i++) {
                    var loadedEntity = mc.theWorld.loadedEntityList.get(i);
                    positions[i] = loadedEntity.getEntityBoundingBox().getPosition();
                    if (loadedEntity instanceof EntityOtherPlayerMP mp) {
                        loadedEntity.setPosition(mp.otherPlayerMPX, mp.otherPlayerMPY, mp.otherPlayerMPZ);
                    }
                }
            }
            MovingObjectPosition movingObjectPosition = entity.rayTrace(d0, 1f);
            double d1 = d0;
            Vec3 vec3 = entity.getPositionEyes(1f);
            boolean flag = false;

            if (mc.playerController.extendedReach()) {
                d0 = 6.0D;
                d1 = 6.0D;
            } else {
                if (d0 > 3.0D) {
                    flag = true;
                }

            }

            if (movingObjectPosition != null) {
                d1 = movingObjectPosition.hitVec.distanceTo(vec3);
            }

            Vec3 vec31 = entity.getLook(1f);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            Entity pointedEntity = null;
            Vec3 vec33 = null;
            float f = 1.0F;
            List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
            double d2 = d1;

            for (Entity o : list) {
                float f1 = o.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = o.getEntityBoundingBox().expand(f1, f1, f1);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if (axisalignedbb.isVecInside(vec3)) {
                    if (d2 >= 0.0D) {
                        pointedEntity = o;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        boolean flag2 = false;

                        if (o == entity.ridingEntity && !flag2) {
                            if (d2 == 0.0D) {
                                pointedEntity = o;
                                vec33 = movingobjectposition.hitVec;
                            }
                        } else {
                            pointedEntity = o;
                            vec33 = movingobjectposition.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }

            if (pointedEntity != null && flag && vec3.distanceTo(vec33) > 3.0D) {
                pointedEntity = null;
                movingObjectPosition = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, null, new BlockPos(vec33));
            }

            if (pointedEntity != null && (d2 < d1 || movingObjectPosition == null)) {
                movingObjectPosition = new MovingObjectPosition(pointedEntity, vec33);
            }
            base.rotationYawHead = lastYaw;
            base.rotationPitchHead = lastPitch;

            if (useRawPosition) {
                for (int i = 0; i < positions.length; i++) {
                    var oldPos = positions[i];
                    mc.theWorld.loadedEntityList.get(i).setPosition(oldPos.xCoord, oldPos.yCoord, oldPos.zCoord);
                }
            }
            return movingObjectPosition;
        }
        return null;
    }

    public static boolean isPassable(double x, double z) {
        BlockPos pos = new BlockPos(mc.thePlayer.posX+x, mc.thePlayer.posY, mc.thePlayer.posZ+z);
        IBlockState state = mc.theWorld.getBlockState(pos);
        Block block = state.getBlock();
        return block instanceof BlockSlab || block instanceof BlockStairs || block instanceof BlockAir || block instanceof BlockStainedGlass;
    }

    public enum Armor {
        HELMET, CHEST, LEGS, BOOTS;
        public static final Armor[] VALUES = values();
    }

    public enum Face8Direction {
        NONE, NORTH, NORTH_EAST, EAST, SOUTH_EAST, SOUTH, SOUTH_WEST, WEST, NORTH_WEST;
        public static final Face8Direction[] VALUES = values();

        public boolean isOpposite(Face8Direction direction) {
            boolean b1 = this == NORTH && direction == SOUTH;
            boolean b2 = this == SOUTH && direction == NORTH;
            boolean b3 = this == WEST && direction == EAST;
            boolean b4 = this == EAST && direction == WEST;
            boolean b5 = this == NORTH_EAST && direction == SOUTH_WEST;
            boolean b6 = this == SOUTH_WEST && direction == NORTH_EAST;
            boolean b7 = this == SOUTH_EAST && direction == NORTH_WEST;
            boolean b8 = this == NORTH_WEST && direction == SOUTH_EAST;
            return b1 || b2 || b3 || b4 || b5 || b6 || b7 || b8;
        }
        public boolean isOpposite(EnumFacing direction) {
            boolean b1 = this == NORTH && direction == EnumFacing.SOUTH;
            boolean b2 = this == SOUTH && direction == EnumFacing.NORTH;
            boolean b3 = this == WEST && direction == EnumFacing.EAST;
            boolean b4 = this == EAST && direction == EnumFacing.WEST;
            return b1 || b2 || b3 || b4;
        }
        public boolean isSame(EnumFacing direction) {
            boolean b1 = this == SOUTH && direction == EnumFacing.SOUTH;
            boolean b2 = this == NORTH && direction == EnumFacing.NORTH;
            boolean b3 = this == EAST && direction == EnumFacing.EAST;
            boolean b4 = this == WEST && direction == EnumFacing.WEST;
            return b1 || b2 || b3 || b4;
        }
    }

    public static <T> List<T> cloneList(List<T> list) {
        return new ArrayList<>(list);
    }

    public static class Enchant {
        private int id,lvl;
        private Enchantment enchantment;

        public Enchant(Enchantment enchantment,int id, int lvl) {
            this.enchantment = enchantment;
            this.id = id;
            this.lvl = lvl;
        }

        public int lvl() {
            return lvl;
        }

        public int id() {
            return id;
        }

        public Enchantment enchantment() {
            return enchantment;
        }
    }
}
