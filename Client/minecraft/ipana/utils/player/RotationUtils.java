package ipana.utils.player;

import ipana.events.EventPreUpdate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.util.*;

public class RotationUtils {
    private static Minecraft mc = Minecraft.getMinecraft();

    public static float[] getRotationNeededForBlock(EntityPlayer paramEntityPlayer, BlockPos pos) {
        double d1 = pos.getX()+0.5 - paramEntityPlayer.posX;
        double d2 = pos.getY() + 0.5D - (paramEntityPlayer.posY + paramEntityPlayer.getEyeHeight());
        double d3 = pos.getZ()+0.5 - paramEntityPlayer.posZ;
        double d4 = Math.sqrt(d1 * d1 + d3 * d3);
        float f1 = ((float) ((Math.atan2(-d3, -d1)) * (180.0D / Math.PI)) - 90.0F);
        float f2 = (float) -((Math.atan2(d2, d4)) * (180.0D / Math.PI));
        return new float[]{f1, f2};
    }

    public static float getDistanceBetweenAngles(float angle1, float angle2) {
        float angle3 = Math.abs(angle1 - angle2) % 360.0F;
        if (angle3 > 180.0F) {
            angle3 = 360.0F - angle3;
        }

        return angle3;
    }
    public static float[] getRotations(final EntityLivingBase entity, float r1, float r2) {
        float pitch;
        final EntityPlayerSP player = mc.thePlayer;
        final double xDist = entity.posX - player.posX;
        final double zDist = entity.posZ - player.posZ;
        double yDist = entity.posY - player.posY;
        final double dist = StrictMath.sqrt(xDist * xDist + zDist * zDist);
        final AxisAlignedBB entityBB = entity.getEntityBoundingBox().expand(0.10000000149011612D, 0.10000000149011612D, 0.10000000149011612D);
        final double playerEyePos = player.posY + player.getEyeHeight();
        final boolean close = false;
        if (close && playerEyePos > entityBB.minY)
            pitch = 60.0F;
        else {
            yDist = (playerEyePos > entityBB.maxY) ? (entityBB.maxY - playerEyePos) : ((playerEyePos < entityBB.minY) ? (entityBB.minY - playerEyePos) : 0.0D);
            pitch = (float) -(StrictMath.atan2(yDist, dist) * 57.29577951308232D);
        }
        float yaw = (float) (StrictMath.atan2(zDist, xDist) * 57.29577951308232D) - 90.0F;
        if (close) {
            final float inc = (dist < 2) ? r1 : r2;
            yaw = (Math.round(yaw / inc) * inc);
        }
        return new float[] { yaw, pitch };
    }
    public static float[] getRotations(EntityLivingBase ent) {
        return getRotationFromPosition(ent.posX, ent.posZ, ent.posY);
    }
    public static float[] getRotationsForAura(EntityLivingBase ent) {
        double playerX = mc.thePlayer.posX;
        double playerY = mc.thePlayer.posY + mc.thePlayer.getEyeHeight();
        double playerZ = mc.thePlayer.posZ;

        double targetX = ent.posX;
        double targetY = ent.posY;
        double targetZ = ent.posZ;
        if (ent instanceof EntityOtherPlayerMP mp) {
            targetX = mp.otherPlayerMPX;
            targetY = mp.otherPlayerMPY;
            targetZ = mp.otherPlayerMPZ;
        }
        targetY += ent.height / 2;

        double xDiff = targetX - playerX;
        double yDiff = targetY - playerY;
        double zDiff = targetZ - playerZ;
        double dist = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float) (Math.atan2(zDiff, xDiff) * 180 / Math.PI - 90);
        float pitch = (float) (-Math.atan2(yDiff, dist) * 180 / Math.PI);
        return new float[]{yaw, pitch};
    }
    public static float[] getRotationFromPosition(double x, double z, double y) {
        double xDiff = x - Minecraft.getMinecraft().thePlayer.posX;
        double zDiff = z - Minecraft.getMinecraft().thePlayer.posZ;
        double yDiff = y - Minecraft.getMinecraft().thePlayer.posY - 0.6;
        double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float) ((Math.atan2(zDiff, xDiff) * 180.0D / (Math.PI)) - 90.0F);
        float pitch = (float) (-(Math.atan2(yDiff, dist) * 180.0D / (Math.PI)));
        return new float[]{yaw, pitch};
    }
    public static float[] getRotationFromPosition(double x, double z, double y, double x2, double y2, double z2) {
        double xDiff = x - x2;
        double zDiff = z - z2;
        double yDiff = y - y2 - 0.6D;
        double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float) ((Math.atan2(zDiff, xDiff) * 180.0D / (Math.PI)) - 90.0F);
        float pitch = (float) (-(Math.atan2(yDiff, dist) * 180.0D / (Math.PI)));
        return new float[]{yaw, pitch};
    }
    public static float[] getRotationFromPosition(double x, double z, double y, EntityLivingBase ent) {
        double xDiff = x - ent.posX;
        double zDiff = z - ent.posZ;
        double yDiff = y - ent.posY - 0.6D;
        double dist = MathHelper.sqrt_double(xDiff * xDiff + zDiff * zDiff);
        float yaw = (float) ((Math.atan2(zDiff, xDiff) * 180.0D / (Math.PI)) - 90.0F);
        float pitch = (float) (-(Math.atan2(yDiff, dist) * 180.0D / (Math.PI)));
        return new float[]{yaw, pitch};
    }
    public static float[] getDirectionToBlock(int var0, int var1, int var2, EnumFacing var3) {
        EntityEgg var4 = new EntityEgg(mc.theWorld);
        var4.posX = (var0 + 0.5D);
        var4.posY = (var1 + 0.5D);
        var4.posZ = (var2 + 0.5D);
        var4.posX += var3.getDirectionVec().getX() * 0.25D;
        var4.posY += var3.getDirectionVec().getY() * 0.25D;
        var4.posZ += var3.getDirectionVec().getZ() * 0.25D;
        return getDirectionToEntity(var4);
    }

    public static float[] getDirectionToEntity(Entity var0) {
        return new float[]{getYaw(var0) + mc.thePlayer.rotationYaw, getPitch(var0) + mc.thePlayer.rotationPitch};
    }

    public static float yaw(float prevYaw, Entity var0) {
        double deltaX = var0.posX - mc.thePlayer.posX;
        double deltaZ = var0.posZ - mc.thePlayer.posZ;
        double yawToEntity;
        double v = Math.toDegrees(Math.atan(deltaZ / deltaX));
        if ((deltaZ < 0.0D) && (deltaX < 0.0D)) {
            yawToEntity = 90.0D + v;
        } else if ((deltaZ < 0.0D) && (deltaX > 0.0D)) {
            yawToEntity = -90.0D + v;
        } else {
            yawToEntity = Math.toDegrees(-Math.atan(deltaX / deltaZ));
        }
        return MathHelper.wrapAngleTo180_float(-(prevYaw - (float)yawToEntity));
    }
    public static float pitch(float prevPitch, Entity var0) {
        double var1 = var0.posX - mc.thePlayer.posX;
        double var3 = var0.posZ - mc.thePlayer.posZ;
        double var5 = var0.posY - 1.6D + var0.getEyeHeight() - mc.thePlayer.posY;
        double var7 = MathHelper.sqrt_double(var1 * var1 + var3 * var3);
        double var9 = -Math.toDegrees(Math.atan(var5 / var7));
        return -MathHelper.wrapAngleTo180_float(prevPitch - (float) var9);
    }

    public static float getYaw(Entity var0) {
        double var1 = var0.posX - mc.thePlayer.posX;
        double var3 = var0.posZ - mc.thePlayer.posZ;
        double var5;
        double v = Math.toDegrees(Math.atan(var3 / var1));
        if ((var3 < 0.0D) && (var1 < 0.0D)) {
            var5 = 90.0D + v;
        } else {
            if ((var3 < 0.0D) && (var1 > 0.0D)) {
                var5 = -90.0D + v;
            } else {
                var5 = Math.toDegrees(-Math.atan(var1 / var3));
            }
        }
        return MathHelper.wrapAngleTo180_float(-(mc.thePlayer.rotationYaw - (float) var5));
    }

    public static float getPitch(Entity var0) {
        double var1 = var0.posX - mc.thePlayer.posX;
        double var3 = var0.posZ - mc.thePlayer.posZ;
        double var5 = var0.posY - 1.6D + var0.getEyeHeight() - mc.thePlayer.posY;
        double var7 = MathHelper.sqrt_double(var1 * var1 + var3 * var3);
        double var9 = -Math.toDegrees(Math.atan(var5 / var7));
        return -MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationPitch - (float) var9);
    }
    public static float yawTo(float prevYaw, double x, double z) {
        double deltaX = x - mc.thePlayer.posX;
        double deltaZ = z - mc.thePlayer.posZ;
        double yawToPos;
        double v = Math.toDegrees(Math.atan(deltaZ / deltaX));
        if ((deltaZ < 0.0D) && (deltaX < 0.0D)) {
            yawToPos = 90.0D + v;
        } else if ((deltaZ < 0.0D) && (deltaX > 0.0D)) {
            yawToPos = -90.0D + v;
        } else {
            yawToPos = Math.toDegrees(-Math.atan(deltaX / deltaZ));
        }
        return MathHelper.wrapAngleTo180_float(-(prevYaw - (float)yawToPos));
    }
    public static float getYawChange(Entity entity, float yaw) {
        double deltaX = entity.posX - mc.thePlayer.posX;
        double deltaZ = entity.posZ - mc.thePlayer.posZ;
        double yawToEntity;
        double v = Math.toDegrees(Math.atan(deltaZ / deltaX));
        if ((deltaZ < 0.0D) && (deltaX < 0.0D)) {
            yawToEntity = 90.0D + v;
        } else if ((deltaZ < 0.0D) && (deltaX > 0.0D)) {
            yawToEntity = -90.0D + v;
        } else {
            yawToEntity = Math.toDegrees(-Math.atan(deltaX / deltaZ));
        }
        return MathHelper.wrapAngleTo180_float(-(yaw - (float)yawToEntity));
    }
    public static float getPitchChange(Entity var0, float pitch) {
        double var1 = var0.posX - mc.thePlayer.posX;
        double var3 = var0.posZ - mc.thePlayer.posZ;
        double var5 = var0.posY - 1.6D + var0.getEyeHeight() - mc.thePlayer.posY;
        double var7 = MathHelper.sqrt_double(var1 * var1 + var3 * var3);
        double var9 = -Math.toDegrees(Math.atan(var5 / var7));
        return (float)var9 - pitch;
    }
}
