package ipana.renders.ide.panels.code;

import ipana.Ipana;
import ipana.eventapi.EventManager;
import ipana.events.EventTick;
import ipana.renders.ide.Panel;
import ipana.renders.ide.panels.pack.item.ClassItem;
import ipana.utils.font.FontHelper;
import ipana.utils.font.FontUtil;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ChatAllowedCharacters;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

public class CodePanel extends Panel {

    public static final CodePanel INSTANCE = new CodePanel();
    private static final Color HOVERED_LINE = new Color(255,255,255,100);

    public CodePanel() {
        super(200,0,"window","window");
        EventManager.eventSystem.subscribeAll(this);
    }
    private ClassItem classItem;
    private final FontUtil font = FontHelper.SIZE_18;
    private int cursorBlinkTicks;


    @Override
    public void initPanel() {

    }

    private Listener<EventTick> onTick = new Listener<>(event -> {
        cursorBlinkTicks++;
    });

    @Override
    public void draw(int mouseX, int mouseY) {
        updateSizes();
        if (classItem != null) {
            ScaledResolution sr = RenderUtils.SCALED_RES;
            Gui.drawRect(200,0,sr.getScaledWidth(), sr.getScaledHeight(), new Color(Ipana.getClientColor().getRed(),Ipana.getClientColor().getGreen(),Ipana.getClientColor().getBlue(),150));
            int lineHeight = 12;
            int y = 5;
            int lineCount = 0;
            for (Line line : classItem.getCode().getLines()) {
                if (line.needCompile()) {
                    line.setColored(colorMotds(line.raw()));
                    line.setNeedCompile(false);
                }
                if (lineCount == classItem.selectedLine()) {
                    Gui.drawRect(200,y,sr.getScaledWidth(), y+12, HOVERED_LINE);
                }
                font.drawString(String.valueOf(lineCount+1), 201, y+3, Color.white.getRGB());
                font.drawStringWithShadow(line.colored(), 220, y+2, Color.white.getRGB());
                if (lineCount == classItem.selectedLine() && cursorBlinkTicks % 20 <= 10) {
                    double cursorX = 219+font.getWidth(line.raw().substring(0, classItem.cursor()));
                    font.drawLine(new Vector2d(cursorX, y), new Vector2d(cursorX, y+12), 2);
                }
                y+=lineHeight;
                lineCount++;
            }
        }
    }

    @Override
    public void keyPressed(int key, char typedChar) {
        if (classItem != null && classItem.selectedLine() >= 0 && getActiveScreens().isEmpty()) {
            Line line = classItem.getCode().getLines().get(classItem.selectedLine());
            if (key == Keyboard.KEY_BACK) {
                String spaces = getSpaces(line.raw());
                if (classItem.cursor()-spaces.length() > 0) {
                    line.changeText(line.raw().substring(0, classItem.cursor()-1)+line.raw().substring(classItem.cursor()));
                    classItem.setCursor(classItem.cursor()-1);
                } else {
                    if (classItem.selectedLine() > 0) {
                        if (line.raw().replace(" ", "").isEmpty()) {
                            classItem.getCode().getLines().remove(line);
                            classItem.setSelectedLine(classItem.selectedLine() - 1);
                            classItem.setCursor(classItem.getCode().getLines().get(classItem.selectedLine()).raw().length());
                        } else {
                            String copy = line.raw().substring(spaces.length());
                            classItem.setSelectedLine(classItem.selectedLine() - 1);
                            Line previousLine = classItem.getCode().getLines().get(classItem.selectedLine());
                            classItem.setCursor(previousLine.raw().length());
                            System.out.println(previousLine.raw()+copy);
                            previousLine.changeText(previousLine.raw()+copy);
                            classItem.getCode().getLines().remove(line);
                        }
                    }
                }
            } else if (key == Keyboard.KEY_RETURN) {
                String spaces = getSpaces(line.raw());
                if (classItem.cursor() == line.raw().length()) {
                    Line nextLine = addLine();
                    classItem.setCursor(getSpaces(line.raw()).length());
                } else if (classItem.cursor()-getSpaces(line.raw()).length() == 0) {
                    Line nextLine = addLine();
                    classItem.setCursor(nextLine.raw().length());
                    nextLine.changeText(line.raw());
                    line.changeText(getSpaces(line.raw()));
                } else {
                    Line nextLine = addLine();
                    String leftOver = line.raw().substring(classItem.cursor());
                    line.changeText(line.raw().substring(0, classItem.cursor()));
                    nextLine.changeText(spaces+leftOver+nextLine.raw());
                    classItem.setCursor(getSpaces(nextLine.raw()).length());
                }
            } else if (key == Keyboard.KEY_TAB) {
                String spacesFrom = getSpacesFrom(line.raw(),classItem.cursor());
                int spaceLength = spacesFrom.length() % 4 == 0 ? 4 : spacesFrom.length() % 4;
                String spaced = " ".repeat(spaceLength);
                line.changeText(line.raw().substring(0, classItem.cursor())+spaced+line.raw().substring(classItem.cursor()));
                classItem.setCursor(classItem.cursor()+spaceLength);
            } else if (key == Keyboard.KEY_UP) {
                classItem.setSelectedLine(Math.max(classItem.selectedLine() - 1, 0));
                Line nextLine = classItem.getCode().getLines().get(classItem.selectedLine());
                classItem.setCursor(Math.min(classItem.cursor(), nextLine.raw().length()));
            } else if (key == Keyboard.KEY_DOWN) {
                classItem.setSelectedLine(Math.min(classItem.selectedLine() + 1, classItem.getCode().getLines().size()-1));
                Line nextLine = classItem.getCode().getLines().get(classItem.selectedLine());
                classItem.setCursor(Math.min(classItem.cursor(), nextLine.raw().length()));
            } else if (key == Keyboard.KEY_RIGHT) {
                classItem.setCursor(Math.min(classItem.cursor()+1, line.raw().length()));
            } else if (key == Keyboard.KEY_LEFT) {
                classItem.setCursor(Math.max(classItem.cursor()-1, 0));
            } else {
                if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                    line.changeText(line.raw().substring(0, classItem.cursor()) + typedChar + line.raw().substring(classItem.cursor()));
                    classItem.setCursor(Math.min(classItem.cursor() + 1, line.raw().length()));
                }
            }
        }
    }

    private Line addLine() {
        Line line = classItem.getCode().getLines().get(classItem.selectedLine());
        classItem.getCode().getLines().add(new Line(getSpaces(line.raw())));
        classItem.setSelectedLine(classItem.selectedLine() + 1);
        sortList(getSpaces(line.raw()));
        return classItem.getCode().getLines().get(classItem.selectedLine());
    }

    private void sortList(String spaces) {
        for (int i = classItem.getCode().getLines().size()-1; i > classItem.selectedLine(); i--) {
            classItem.getCode().getLines().set(i, new Line(classItem.getCode().getLines().get(i-1).raw()));
        }
        classItem.getCode().getLines().set(classItem.selectedLine(), new Line(spaces));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (classItem != null) {
            int lineHeight = 12;
            int y = 5;
            int lineCount = 0;
            for (Line line : classItem.getCode().getLines()) {
                if (mouseX > 200 && mouseY >= y && mouseY < y+lineHeight) {
                    cursorBlinkTicks = 0;
                    classItem.setCursor(setCursorPos(line, mouseX));
                    classItem.setSelectedLine(lineCount);
                    break;
                }
                y+=lineHeight;
                lineCount++;
            }
        }
    }

    private int setCursorPos(Line line, int mouseX) {
        float x = 220;
        if (mouseX < x) {
            return 0;
        } else if (mouseX > x+font.getWidth(line.raw())) {
            return line.raw().length();
        }
        for (int i = 0; i <= line.raw().length(); i++) {
            if (x+font.getWidth(line.raw().substring(0,i)) > mouseX) {
                return i;
            }
        }
        return 0;
    }
    private String getSpaces(String raw) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            if (raw.charAt(i) == ' ') {
                builder.append(" ");
            } else {
                return builder.toString();
            }
        }
        return builder.toString();
    }
    private String getSpacesFrom(String raw, int cursor) {
        StringBuilder builder = new StringBuilder();
        for (int i = cursor-1; i >= 0; i--) {
            if (raw.charAt(i) == ' ') {
                builder.append(" ");
            } else {
                return builder.toString();
            }
        }
        return builder.toString();
    }

    private String colorMotds(String line) {
        String[] motds = new String[]{"package","import","static","public","private","class","void","boolean","int","double","float","short","long","final","return","extends","char","for","super","else","if","new", "true","false","instanceof"};
        String colored = line;
        for (String motd : motds) {
            colored = replace(colored, motd);
        }
        boolean stringBegin = false;
        for (int i = 0; i < colored.length(); i++) {
            char c = colored.charAt(i);
            if (c == '"') {
                if (!stringBegin) {
                    colored = colored.substring(0, i) + "§a" +'"'+colored.substring(i+1);
                    i+=3;
                    stringBegin = true;
                } else {
                    colored = colored.substring(0, i) +'"'+ "§f" +colored.substring(i+1);
                    i+=3;
                    stringBegin = false;
                }
            }
        }
        return colored;
    }

    private String replace(String line, String motd) {
        String replaced = line;
        int i = 0;
        int size = line.length()-motd.length();
        while (i < size) {
            String substring = replaced.substring(i,i+motd.length());
            boolean startBound = i-1 < 0 || isTroll(replaced.charAt(i-1));
            boolean endBound = i+motd.length() > replaced.length() || isTroll(replaced.charAt(i+motd.length()));
            if (startBound && endBound && substring.equals(motd)) {
                replaced = replaced.substring(0, i) + "§6"+motd+"§f"+ replaced.substring(i+motd.length());
                size+=2;
            }
            i++;
        }
        return replaced;
    }

    private boolean isTroll(char c) {
        String troll = " ()[]{}=;";
        return troll.contains(String.valueOf(c));
    }

    public void setClassItem(ClassItem classItem) {
        this.classItem = classItem;
    }
}