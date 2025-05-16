package ipana.utils.render;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ColorUtil {
    public final static ColorUtil INSTANCE = new ColorUtil();
    private final static List<Color> STRAIGHT_LINE = new ArrayList<>();

    public void initColorTables() {
        initStraightLineTables();
    }

    public static List<Color> straightLine() {
        return STRAIGHT_LINE;
    }
    private void initStraightLineTables() {
        int r = 50;
        int g = 50;
        int b = 200;
        while (b < 255) {
            STRAIGHT_LINE.add(new Color(r,g,b));
            b+=5;
        }
        while (g < 255) {
            STRAIGHT_LINE.add(new Color(r,g,b));
            g+=15;
        }
        g = 255;
        while (r < 255) {
            if (g > 0) {
                g-=15;
            }
            STRAIGHT_LINE.add(new Color(r,g,b));
            r+=5;
        }
    }
}
