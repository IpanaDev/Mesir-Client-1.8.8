package ipana.renders.settings;

import ipana.managements.friend.Friend;
import ipana.managements.friend.FriendManager;
import ipana.utils.config.ConfigUtils;
import ipana.utils.font.FontHelper;
import ipana.utils.font.FontUtil;
import ipana.utils.render.ColorUtil;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;

public class FriendGui extends GuiScreen {
    private FontUtil font = FontHelper.SIZE_18;
    private int page = 1;
    private GuiTextField textField = new GuiTextField(31, Minecraft.getMinecraft().fontRendererObj,0,0,0,0);
    private long ms;
    private boolean cursorBlink;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = RenderUtils.SCALED_RES;
        Color backGround = new Color(54,57,63);
        Color hoverColor = new Color(255, 255, 255, 120);
        int x = sr.getScaledWidth()/2-100;
        int y = sr.getScaledHeight()/2-100;
        int width = 200;
        int height = 260;
        RenderUtils.drawRoundedRect(x,y,width,height,3f,backGround);
        font.drawString("Friend List ("+FriendManager.getFriends().size()+")",x+5,y+6.5f,Color.white.getRGB());
        int maxSize = 17;
        int maxPages = (int) Math.ceil((double)FriendManager.getFriends().size()/(double) maxSize);
        page = Math.min(maxPages,page);
        page = Math.max(page,1);
        font.drawString("<",x+2,y+height-22,Color.white.getRGB());
        font.drawString(">",x+width-font.getWidth(">")-2,y+height-22,Color.white.getRGB());
        font.drawString(page+"/"+maxPages,x+(width/2f)-font.getWidth(page+"/"+maxPages),y+height-22,Color.white.getRGB());
        font.drawString(textField.getText(),x+2,y+height-10,Color.white.getRGB());
        if (cursorBlink) {
            double textWidth = font.getWidth(textField.getText());
            int cursorPos = textField.getCursorPosition();
            double cursorX = textWidth/Math.max(1,textField.getText().length())*cursorPos;
            Gui.drawRect(x+2+cursorX,y+height-3,x+2+cursorX+5,y+height-2,Color.white.getRGB());
        }
        if (System.currentTimeMillis()-ms >= 250) {
            cursorBlink = !cursorBlink;
            ms = System.currentTimeMillis();
        }
        font.drawString("Ok",x+width-font.getWidth("Ok")-2,y+height-10,Color.green.getRGB());
        if (isHovered(mouseX,mouseY,x+1,y+height-24,font.getWidth("<")+2,10)) {
            Gui.drawRect(x+1,y+height-24,x+1+font.getWidth("<")+1,y+height-14,hoverColor.getRGB());
        }
        if (isHovered(mouseX,mouseY,x+width-font.getWidth(">")-2,y+height-24,font.getWidth(">")+1,10)) {
            Gui.drawRect(x+width-font.getWidth(">")-3,y+height-24,x+width-font.getWidth(">")-2+font.getWidth(">")+1,y+height-14,hoverColor.getRGB());
        }
        if (isHovered(mouseX,mouseY,x+width-font.getWidth("Ok")-3,y+height-12,font.getWidth("Ok")+1,10)) {
            Gui.drawRect(x+width-font.getWidth("Ok")-3,y+height-12,x+width-font.getWidth("Ok")-3+font.getWidth("Ok")+1,y+height-2,hoverColor.getRGB());
        }
        double colorWidth = width / (double) ColorUtil.straightLine().size();
        double colorX = x;
        for (Color color : ColorUtil.straightLine()) {
            Gui.drawRect(colorX,y+20,colorX+colorWidth,y+21,color.getRGB());
            colorX+=colorWidth;
        }
        int listY = y+27;
        int sizePerPage = 0;
        for (Friend friend : FriendManager.getFriends()) {
            if (sizePerPage/maxSize == page-1) {
                font.drawString(friend.name, x + 5, listY, Color.white.getRGB());
                float width2 = font.getWidth("X") - 1;
                float height2 = 10;
                float y2 = listY - 2;
                float x2 = x + width - width2 - 2;
                if (isHovered(mouseX, mouseY, x2, y2, width2, height2)) {
                    Gui.drawRect(x2 - 1, y2, x2 + width2 + 1, y2 + height2, hoverColor.getRGB());
                }
                font.drawString("X", x2, listY, Color.red.getRGB());
                listY += 12;
            } else {
                listY = y+27;
            }
            sizePerPage++;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ScaledResolution sr = RenderUtils.SCALED_RES;
        int x = sr.getScaledWidth()/2-100;
        int y = sr.getScaledHeight()/2-100;
        int width = 200;
        int height = 260;
        int maxSize = 17;
        if (isHovered(mouseX,mouseY,x+1,y+height-24,font.getWidth("<")+2,10)) {
            page--;
        }
        if (isHovered(mouseX,mouseY,x+width-font.getWidth(">")-2,y+height-24,font.getWidth(">")+1,10)) {
            page++;
        }
        if (isHovered(mouseX,mouseY,x+width-font.getWidth("Ok")-3,y+height-12,font.getWidth("Ok")+1,10)) {
            System.out.println("za");
            if (textField.getText().length() > 0 && !textField.getText().replace(" ","").equals("") && !isExisted(textField.getText())) {
                FriendManager.add(textField.getText());
                textField.setText("");
            }
        }
        int maxPages = (int) Math.ceil((double)FriendManager.getFriends().size()/(double) maxSize);
        page = Math.max(page,1);
        page = Math.min(maxPages,page);
        int listY = y+27;
        int sizePerPage = 0;
        Friend clickedFriend = null;
        for (Friend friend : FriendManager.getFriends()) {
            if (sizePerPage/maxSize == page-1) {
                double width2 = font.getWidth("X") - 1;
                double height2 = 10;
                double y2 = listY - 2;
                double x2 = x + width - width2 - 2;
                if (isHovered(mouseX, mouseY, x2, y2, width2, height2)) {
                    clickedFriend = friend;
                    break;
                }
                listY += 12;
            } else {
                listY = y+27;
            }
            sizePerPage++;
        }
        if (clickedFriend != null) {
            FriendManager.getFriends().remove(clickedFriend);
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        textField.setFocused(true);
        textField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_RETURN && textField.getText().length() > 0 && !textField.getText().replace(" ","").equals("") && !isExisted(textField.getText())) {
            FriendManager.add(textField.getText());
            textField.setText("");
        }
        if (keyCode == Keyboard.KEY_ESCAPE) {
            ConfigUtils.saveFriends();
        }
        super.keyTyped(typedChar, keyCode);
    }

    private boolean isExisted(String name) {
        boolean exist = false;
        for (Friend friend : FriendManager.getFriends()) {
            if (name.equals(friend.name)) {
                exist = true;
                break;
            }
        }
        return exist;
    }

    private boolean isHovered(int mouseX, int mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX <= x+width && mouseY >= y && mouseY <= y+height;
    }
}