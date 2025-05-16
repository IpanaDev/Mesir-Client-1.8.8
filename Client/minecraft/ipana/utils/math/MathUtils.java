package ipana.utils.math;

import ipana.utils.player.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class MathUtils {
    private static Random RANDOM = new Random();

    public static boolean inBounds(long value, long min, long max) {
        return min <= value && value <= max;
    }

    public static double positive(double value) {
        double newValue = value;
        if (newValue < 0) {
            newValue*=-1;
        }
        return newValue;
    }
    public static double positive(double value1, double value2) {
        double newValue = 0;
        if (value1 >= 0 && value2 <= 0) {
            newValue = value1-positive(value2);
        } else if (value1 <= 0 && value2 >= 0) {
            newValue = value2-positive(value1);
        } else if (value1 >= 0 && value2 >= 0) {
            newValue = value1-value2;
        } else if (value1 <= 0 && value2 <= 0) {
            newValue = positive(value1)-positive(value2);
        }
        return positive(newValue);
    }
    public static int random(int min, int max)
    {
        if (max <= min) {
            return min;
        }
        return RANDOM.nextInt(max - min) + min;
    }

    public static double fixFormat(double value, int places) {
        if (places<0) {
            throw new IllegalArgumentException();
        } else {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.doubleValue();
        }
    }

    public static float fixFormat(float value, int places) {
        if (places<0) {
            throw new IllegalArgumentException();
        } else {
            if (Float.isNaN(value) || Float.isInfinite(value)) {
                return 0;
            }
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(places, RoundingMode.HALF_UP);
            return bd.floatValue();
        }
    }
}
