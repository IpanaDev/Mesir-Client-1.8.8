package ipana.utils.ncp.utilities;

import net.minecraft.client.Minecraft;
import net.minecraft.potion.Potion;

public class Magic {
    public static double oddGravity(double lastYDist, double setBackY, double fromY) {
        return Max(gravity1(lastYDist), gravity3(lastYDist, setBackY, fromY), gravity4(lastYDist), gravity5(lastYDist));
    }

    private static double gravity1(double lastYDist) {
        double b1 = -0.22920000000000001D;
        double b2 = 0.22920000000000001D;

        double y = Double.NEGATIVE_INFINITY;
        if (lastYDist < 0.3126) {
            y = Math.max(y, lastYDist - 0.0624 - 1E-13);
            y = isInBounds(y, b1, b2);
        }

        if (lastYDist > 0.025D && lastYDist < 0.0624D) {
            y = Math.max(y, 0);
            y = isInBounds(y, b1, b2);
        }

        if (lastYDist <= 0.14579999999999999 && lastYDist > 0.05D) {
            y = Math.max(y, 0.05 - 1E-13);
            y = isInBounds(y, b1, b2);
        }

        if (lastYDist < -0.0834D) {
            y = Math.max(y, lastYDist - 0.025 - 1E-13);
            y = isInBounds(y, b1, b2);
        }

        return y;
    }

    private static double gravity3(double lastYDist, double setBackY, double fromY) {
        if (Math.abs(setBackY - fromY) < 1.0) {
            if (lastYDist > 0.0834D && lastYDist < 0.2502D) {
                return lastYDist - 0.05 - 1E-13;
            } else {
                return -1E-13;
            }
        }
        return Double.NEGATIVE_INFINITY;
    }

    private static double gravity4(double lastYDist) {
        if (Minecraft.getMinecraft().thePlayer.isPotionActive(Potion.jump) && lastYDist > -0.198D && lastYDist < 0.11460000000000001D) {
            double y = lastYDist - 0.021 - 1E-13;
            y = isInBounds(y, -0.29156, 0.0624);
            return y;
        }
        return Double.NEGATIVE_INFINITY;
    }

    private static double gravity5(double lastYDist) {
        if (lastYDist > -0.0834D && lastYDist < 0.0624D) {
            return lastYDist - 0.0312 - 1E-13;
        }
        return Double.NEGATIVE_INFINITY;
    }

    private static double isInBounds(double v, double min, double max) {
        if (v > min && v < max) {
            return v;
        }
        return Double.NEGATIVE_INFINITY;
    }

    private static double Max(double... values) {
        double max = Double.NEGATIVE_INFINITY;
        for (double value : values) {
            max = Math.max(max, value);
        }
        return max;
    }
}
