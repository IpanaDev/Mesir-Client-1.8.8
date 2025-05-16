package ipana.shell;

import ipana.eventapi.EventManager;
import ipana.events.EventTick;
import ipana.shell.cmd.Cmd;
import ipana.shell.cmd.CmdManager;
import ipana.shell.cmd.Status;
import ipana.shell.cmd.Written;
import ipana.utils.font.FontHelper;
import ipana.utils.font.FontUtil;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import pisi.unitedmeows.eventapi.event.listener.Listener;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static ipana.shell.ShellValues.*;

public class Shell {
    private String shellName;
    private String user;
    private int x,y;
    private boolean moving;
    private FontUtil font;
    private InnerButton theX;
    private Minecraft mc = Minecraft.getMinecraft();
    public List<Written> writtenList = new ArrayList<>();
    private GuiTextField textField = new GuiTextField(0,mc.fontRendererObj,0,0,0,0);
    private int cursorTicks;
    private int historyIndex;
    private int preMouseX,preMouseY;

    public Shell(String shellName,String user,int x,int y) {
        this.shellName = shellName;
        this.user = user;
        this.x = x;
        this.y = y;
        font = FontHelper.SIZE_15;
        theX = new InnerButton("X",0,0,10,10,font) {
            @Override
            void isClicked(int mouseX, int mouseY) {
                if (isHovered(mouseX, mouseY)) {
                    mc.displayGuiScreen(null);
                }
            }
        };
        textField.setFocused(true);
        textField.setCanLoseFocus(false);
        new CmdManager();
        ShellValues.reload("default");
        EventManager.eventSystem.subscribeAll(this);
    }

    public boolean isHovered(int mouseX,int mouseY) {
        return mouseX>=getX() && mouseY>=getY() && mouseX<=getX()+SHELL_TOP_WIDTH && mouseY<=getY()+SHELL_TOP_HEIGHT;
    }

    public void render(int mouseX,int mouseY) {
        RenderUtils.drawRoundedRect(getX(),getY(),SHELL_TOP_WIDTH,SHELL_TOP_HEIGHT,5.5f,SHELL_TOP_COLOR);

        RenderUtils.drawRoundedRect(getX(),getY()+SHELL_TOP_HEIGHT,SHELL_WIDTH,SHELL_HEIGHT,5.5f,SHELL_COLOR);

        //Fillers
        Gui.drawRect(getX(),getY()+SHELL_TOP_HEIGHT-5,getX()+SHELL_WIDTH,getY()+SHELL_TOP_HEIGHT,SHELL_TOP_COLOR.getRGB());
        Gui.drawRect(getX(),getY()+SHELL_TOP_HEIGHT,getX()+SHELL_WIDTH,getY()+SHELL_TOP_HEIGHT+5,SHELL_COLOR.getRGB());

        font.drawString(getShellName(),getX()+(SHELL_TOP_WIDTH/2f)-font.getWidth(getShellName())/2f,getY()+7,SHELL_NAME_COLOR.getRGB());
        setPosition(mouseX,mouseY);
        theX.renderButton(getX()+SHELL_TOP_WIDTH-12,getY()+2,mouseX,mouseY,new Color(200,200,200,100));
        String serverStatus = mc.isSingleplayer() ? "SP" : "MP";
        String startText = "<"+serverStatus+":/"+getUser()+"> ";
        int y = writtenList.size() > 12 ? (getY()+SHELL_TOP_HEIGHT+5) - (14*(writtenList.size()-12)) : getY()+SHELL_TOP_HEIGHT+5;
        for (Written texts : writtenList) {
            if (y >= getY()+SHELL_TOP_HEIGHT+5 && y <= getY()+SHELL_HEIGHT) {
                if (texts.isSentByShell()) {
                    font.drawString(texts.getString(),getX()+5,y,SHELL_TEXT_COLOR.getRGB());
                } else {
                    font.drawString(startText,getX()+5,y,SHELL_USER_COLOR.getRGB());
                    font.drawString(texts.getString(),getX()+5+font.getWidth(startText),y,SHELL_TEXT_COLOR.getRGB());
                }
            }
            y+=14;
        }

        font.drawString(startText+textField.getText(),getX()+5,y,Color.white.getRGB());
        if (cursorTicks>=5) {
            double width = font.getWidth(startText);
            int cursorPos = textField.getCursorPosition();
            double cursorX = font.getWidth(textField.getText())/Math.max(1,textField.getText().length())*cursorPos;
            int posX = getX()+3;
            Gui.drawRect(posX+width+cursorX,y+5,posX+width+cursorX+5,y+6.5,Color.white.getRGB());
        }
    }

    public void onKey(char typedChar, int key) {
        textField.textboxKeyTyped(typedChar, key);
        if (key == Keyboard.KEY_RETURN) {
            boolean found = false;
            String[] split = textField.getText().split(" ");
            writtenList.add(new Written(textField.getText(),false));
            for (Cmd cmd : CmdManager.getList()) {
                for (String str : cmd.getNames()) {
                    if (split[0].equalsIgnoreCase(str)) {
                        cmd.onCommand(this,split);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                printToShell("Can't find command!",Status.Error);
            }
            textField.setText("");
            historyIndex = writtenList.size();
        } else if (key == Keyboard.KEY_UP) {
            if (writtenList.size() > 0 && historyIndex > 0) {
                int i = historyIndex;
                while (writtenList.get(i-1).isSentByShell()) {
                    i--;
                }
                historyIndex=i-1;
                textField.setText(writtenList.get(historyIndex).getString());
            }
        } else if (key == Keyboard.KEY_DOWN) {
            if (writtenList.size() > 0 && historyIndex < writtenList.size()) {
                int i = historyIndex;
                while (i+1 < writtenList.size() && writtenList.get(i+1).isSentByShell()) {
                    i++;
                }
                historyIndex=i+1;
                if (historyIndex >= writtenList.size()) {
                    while (writtenList.get(historyIndex-1).isSentByShell()) {
                        historyIndex--;
                    }
                    historyIndex-=1;
                }
                textField.setText(writtenList.get(historyIndex).getString());
            }
        }
    }

    public void printToShell(String string, Status status) {
        switch (status) {
            case Error: {
                string = "§4ERROR: §f"+string;
                break;
            }
            case Warning: {
                string = "§dWARNING: §f"+string;
                break;
            }
            case Success: {
                string = "§2SUCCESS: §f"+string;
                break;
            }
            case Info: {
                string = "§bINFO: §f"+string;
                break;
            }
        }
        writtenList.add(new Written(string,true));
    }

    public void whenClicked(int mouseX, int mouseY) {
        theX.isClicked(mouseX, mouseY);
        setMoving(false);
    }

    private void setPosition(int mouseX, int mouseY) {
        if (isMoving()) {
            setX(getX()+(mouseX-preMouseX));
            setY(getY()+(mouseY-preMouseY));
        }
        preMouseX = mouseX;
        preMouseY = mouseY;
    }

    private Listener<EventTick> onTick = new Listener<>(event -> {
        if (cursorTicks--<=0) {
            cursorTicks=10;
        }
    });

    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

    public String getShellName() {
        return shellName;
    }

    public String getUser() {
        return user;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
