package ipana.utils.ncp.utilities;

import net.minecraft.entity.Entity;

import javax.vecmath.Vector3d;

public class TrigUtil {
    public static double directionCheck(Entity source, double eyeHeight, Vector3d dir, double targetX, double targetY, double targetZ, double targetWidth, double targetHeight, double precision) {
        return directionCheck(source.posX, source.posY + eyeHeight, source.posZ, dir.getX(), dir.getY(), dir.getZ(), targetX, targetY, targetZ, targetWidth, targetHeight, precision);
    }

    public static double directionCheck(double sourceX, double sourceY, double sourceZ, double dirX, double dirY, double dirZ, double targetX, double targetY, double targetZ, double targetWidth, double targetHeight, double precision) {
        double dirLength = Math.sqrt(dirX * dirX + dirY * dirY + dirZ * dirZ);
        if (dirLength == 0.0D) {
            dirLength = 1.0D;
        }

        double dX = targetX - sourceX;
        double dY = targetY - sourceY;
        double dZ = targetZ - sourceZ;
        double targetDist = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
        double xPrediction = targetDist * dirX / dirLength;
        double yPrediction = targetDist * dirY / dirLength;
        double zPrediction = targetDist * dirZ / dirLength;
        double off = 0.0D;
        off += Math.max(Math.abs(dX - xPrediction) - (targetWidth / 2.0D + precision), 0.0D);
        off += Math.max(Math.abs(dZ - zPrediction) - (targetWidth / 2.0D + precision), 0.0D);
        off += Math.max(Math.abs(dY - yPrediction) - (targetHeight / 2.0D + precision), 0.0D);
        if (off > 1.0D) {
            off = Math.sqrt(off);
        }

        return off;
    }
}
