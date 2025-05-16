package ipana.renders.ide.util;

import ipana.renders.ide.settings.SharedAttributes;

public class Formatter {


    public static double formatWidth(String string) {
        if (string.equalsIgnoreCase("window")) {
            return SharedAttributes.WINDOW_WIDTH;
        } else {
            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
    }
    public static double formatHeight(String string) {
        if (string.equalsIgnoreCase("window")) {
            return SharedAttributes.WINDOW_HEIGHT;
        } else {
            try {
                return Double.parseDouble(string);
            } catch (NumberFormatException ex) {
                return 0;
            }
        }
    }
}
