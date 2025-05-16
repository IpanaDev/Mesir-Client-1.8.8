package net.minecraft.client.entity;

import com.mojang.authlib.GameProfile;
import ipana.utils.net.Pinger;
import ipana.utils.player.PlayerUtils;
import ipana.utils.player.RotationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;

public class EntityOtherPlayerMP extends AbstractClientPlayer {
    private boolean isItemInUse;
    public int otherPlayerMPPosRotationIncrements;
    public double otherPlayerMPX, prevMPX;
    public double otherPlayerMPY, prevMPY;
    public double otherPlayerMPZ, prevMPZ;
    public double otherPlayerMPYaw;
    public double otherPlayerMPPitch;
    private float lastAngle;
    private boolean shouldUpdate;
    private ArrayList<PlayerPrediction> predictions = new ArrayList<>();

    public EntityOtherPlayerMP(World worldIn, GameProfile gameProfileIn) {
        super(worldIn, gameProfileIn);
        this.stepHeight = 0.0F;
        this.noClip = true;
        this.renderOffsetY = 0.25F;
        this.renderDistanceWeight = 10.0D;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return true;
    }

    public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean p_180426_10_) {
        this.prevMPX = this.otherPlayerMPX;
        this.prevMPY = this.otherPlayerMPY;
        this.prevMPZ = this.otherPlayerMPZ;
        this.otherPlayerMPX = x;
        this.otherPlayerMPY = y;
        this.otherPlayerMPZ = z;
        this.otherPlayerMPYaw = yaw;
        this.otherPlayerMPPitch = pitch;
        this.otherPlayerMPPosRotationIncrements = posRotationIncrements;


        int predictSize = 3;
        double ping = 1.5;
        this.predictions.clear();
        /*double xOff = (this.otherPlayerMPX - this.prevMPX) * ping / predictSize;
        double yOff = (this.otherPlayerMPY - this.posY) / predictSize;
        double zOff = (this.otherPlayerMPZ - this.prevMPZ) * ping / predictSize;*/

        /*double xOff = (this.otherPlayerMPX - this.posX) * ping / predictSize;
        double yOff = (this.otherPlayerMPY - this.posY) / predictSize;
        double zOff = (this.otherPlayerMPZ - this.posZ) * ping / predictSize;

        this.predictions.clear();
        for (int i = 0; i < predictSize; i++) {
            int count = i + 1;
            double predictedX = this.posX + xOff * count;
            double predictedY = this.posY + yOff * count;
            double predictedZ = this.posZ + zOff * count;

            predictions.add(new PlayerPrediction(predictedX, predictedY, predictedZ, Minecraft.getRunTick()));
        }
        shouldUpdate = true;*/
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate() {
        this.renderOffsetY = 0.0F;
        super.onUpdate();

        if (Float.isNaN(limbSwingAmount) || Float.isNaN(prevLimbSwingAmount) || Float.isNaN(limbSwing)) {
            limbSwingAmount = 0;
            limbSwing = 0;
        }
        this.prevLimbSwingAmount = this.limbSwingAmount;
        double d0 = this.posX - this.prevPosX;
        double d1 = this.posZ - this.prevPosZ;
        float f = (float) (MathHelper.sqrt_double(d0 * d0 + d1 * d1) * 4.0F);
        if (f > 1.0F) {
            f = 1.0F;
        }
        this.limbSwingAmount += (f - this.limbSwingAmount) * 0.4F;
        this.limbSwing += this.limbSwingAmount;

        ItemStack itemstack = this.inventory.mainInventory[this.inventory.currentItem];
        if (itemInUseCount <= 0 && this.isEating() && itemstack != null) {
            this.setItemInUse(itemstack, itemstack.getItem().getMaxItemUseDuration(itemstack));
        }
        if (itemInUseCount > 0 && !this.isEating()) {
            this.clearItemInUse();
        }
    }

    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    public void onLivingUpdate() {
        if (predictions.isEmpty() && this.otherPlayerMPPosRotationIncrements > 0) {
            double d0 = this.posX + (this.otherPlayerMPX - this.posX) / (double) this.otherPlayerMPPosRotationIncrements;
            double d1 = this.posY + (this.otherPlayerMPY - this.posY) / (double) this.otherPlayerMPPosRotationIncrements;
            double d2 = this.posZ + (this.otherPlayerMPZ - this.posZ) / (double) this.otherPlayerMPPosRotationIncrements;
            double d3;

            d3 = this.otherPlayerMPYaw - (double) this.rotationYaw;
            while (d3 < -180.0D) {
                d3 += 360.0D;
            }

            while (d3 >= 180.0D) {
                d3 -= 360.0D;
            }

            this.rotationYaw = (float) ((double) this.rotationYaw + d3 / (double) this.otherPlayerMPPosRotationIncrements);
            this.rotationPitch = (float) ((double) this.rotationPitch + (this.otherPlayerMPPitch - (double) this.rotationPitch) / (double) this.otherPlayerMPPosRotationIncrements);
            --this.otherPlayerMPPosRotationIncrements;
            this.setPosition(d0, d1, d2);
            this.setRotation(this.rotationYaw, this.rotationPitch);
        }
        if (!predictions.isEmpty()) {
            PlayerPrediction prediction = predictions.getFirst();
            double d3;

            d3 = this.otherPlayerMPYaw - (double) this.rotationYaw;
            while (d3 < -180.0D) {
                d3 += 360.0D;
            }

            while (d3 >= 180.0D) {
                d3 -= 360.0D;
            }

            this.rotationYaw = (float) ((double) this.rotationYaw + d3 / (double) predictions.size());
            this.rotationPitch = (float) ((double) this.rotationPitch + (this.otherPlayerMPPitch - (double) this.rotationPitch) / (double) predictions.size());

            this.setPosition(prediction.x, prediction.y, prediction.z);
            this.setRotation(this.rotationYaw, this.rotationPitch);
            predictions.removeFirst();
            this.otherPlayerMPPosRotationIncrements = 1;
        }


        this.prevCameraYaw = this.cameraYaw;
        this.updateArmSwingProgress();
        float f1 = (float) MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
        float f = (float) Math.atan(-this.motionY * 0.20000000298023224D) * 15.0F;

        if (f1 > 0.1F) {
            f1 = 0.1F;
        }
        if (!this.onGround || this.getHealth() <= 0.0F) {
            f1 = 0.0F;
        }

        if (this.onGround || this.getHealth() <= 0.0F) {
            f = 0.0F;
        }
        this.cameraYaw += (f1 - this.cameraYaw) * 0.4F;
        this.cameraPitch += (f - this.cameraPitch) * 0.8F;
    }

    /**
     * Sets the held item, or an armor slot. Slot 0 is held item. Slot 1-4 is armor. Params: Item, slot
     */
    public void setCurrentItemOrArmor(int slotIn, ItemStack stack) {
        if (slotIn == 0) {
            this.inventory.mainInventory[this.inventory.currentItem] = stack;
        } else {
            this.inventory.armorInventory[slotIn - 1] = stack;
        }
    }

    public float angle(double x, double z, double x2, double z2) {
        double xDiff = x - x2;
        double zDiff = z - z2;
        return (float) ((Math.atan2(zDiff, xDiff) * 180.0D / (Math.PI)) - 90.0F);
    }

    /**
     * Send a chat message to the CommandSender
     */
    public void addChatMessage(IChatComponent component) {
        Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(component);
    }

    /**
     * Returns {@code true} if the CommandSender is allowed to execute the command, {@code false} if not
     */
    public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
        return false;
    }

    /**
     * Get the position in the world. <b>{@code null} is not allowed!</b> If you are not an entity in the world, return
     * the coordinates 0, 0, 0
     */
    public BlockPos getPosition() {
        return new BlockPos(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D);
    }

    class PlayerPrediction {
        double x, y, z;
        int tick;

        public PlayerPrediction(double x, double y, double z, int tick) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.tick = tick;
        }
    }
}
