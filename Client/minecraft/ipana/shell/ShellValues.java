package ipana.shell;

import ipana.utils.config.ConfigUtils;
import ipana.utils.file.FileUtils;
import stelixobject.objectfile.SxfDataObject;
import stelixobject.objectfile.SxfFile;
import stelixobject.objectfile.reader.SXfReader;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class ShellValues {
    public static int SHELL_TOP_WIDTH;
    public static int SHELL_TOP_HEIGHT;
    public static int SHELL_WIDTH;
    public static int SHELL_HEIGHT;

    public static Color SHELL_TOP_COLOR;
    public static Color SHELL_NAME_COLOR;
    public static Color SHELL_USER_COLOR;
    public static Color SHELL_TEXT_COLOR;
    public static Color SHELL_COLOR;
    public static Color SHELL_BACK_COLOR;

    public static void reload(String themeName) {
        SHELL_TOP_WIDTH = 325;
        SHELL_TOP_HEIGHT= 20;
        SHELL_WIDTH = 325;
        SHELL_HEIGHT = 185;

        load(themeName);
    }
    public static void load(String themeName) {
        try {
            final List<String> fileContent = FileUtils.read(FileUtils.getConfigFile("theme_"+themeName));
            for (final String line : fileContent) {
                final String[] color = line.substring(line.indexOf("[")+1,line.indexOf("]")).split(",");
                int index = fileContent.indexOf(line);
                Color newColor = new Color(Integer.parseInt(color[0]),Integer.parseInt(color[1]),Integer.parseInt(color[2]),Integer.parseInt(color[3]));
                if (index == 0)
                    SHELL_TEXT_COLOR = newColor;
                else if (index == 1)
                    SHELL_USER_COLOR = newColor;
                else if (index == 2)
                    SHELL_NAME_COLOR = newColor;
                else if (index == 3)
                    SHELL_TOP_COLOR = newColor;
                else if (index == 4)
                    SHELL_COLOR = newColor;
                else if (index == 5)
                    SHELL_BACK_COLOR = newColor;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
