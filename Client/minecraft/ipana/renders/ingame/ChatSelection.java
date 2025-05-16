package ipana.renders.ingame;

import ipana.Ipana;
import ipana.irc.IRC;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.MathHelper;

import java.awt.*;

import static net.minecraft.client.gui.Gui.drawRect;

public class ChatSelection {
    private static final Color GRADIENT_1 = new Color(255, 1, 1);
    private static final Color GRADIENT_2 = new Color(255, 1, 1, 0);
    private static final Color COLOR = new Color(31, 104, 25, 150);
    private Minecraft mc = Minecraft.getMinecraft();
    private FontRenderer fontRenderer = mc.fontRendererObj;
    public boolean comboBox;
    private GuiChat chat;

    public ChatSelection(GuiChat chat) {
        this.chat = chat;
    }

    public void draw(int mouseX, int mouseY) {
        String chatName = mc.ingameGUI.currentChat.chatName();
        int nameWidth = fontRenderer.getStringWidth(chatName);
        boolean hovered = mouseX > 2 && mouseX < 4 + nameWidth && mouseY > chat.height - 28 && mouseY < chat.height - 16;
        drawRect(2, chat.height - 28, 6 + nameWidth, chat.height - 16, hovered ? Integer.MAX_VALUE : Integer.MIN_VALUE);
        fontRenderer.drawStringWithShadow(chatName, 3, chat.height - 26, Color.white.getRGB());
        if (comboBox) {
            int y = chat.height - 28 - 12*mc.ingameGUI.chats().size();
            int maxWidth = maxWidth()+35;
            for (GuiNewChat bokye : mc.ingameGUI.chats()) {
                chatName = bokye.chatName();
                hovered = mouseX > 2 && mouseX < 4 + maxWidth && mouseY > y && mouseY < y+12;
                drawRect(2, y, 4 + maxWidth, y + 12, hovered ? COLOR.darker() : COLOR);
                fontRenderer.drawStringWithShadow(chatName, 3, y+2, Color.white.getRGB());
                if (bokye.messagesUnseen > 0) {
                    String str = "[+"+bokye.messagesUnseen+"]";
                    fontRenderer.drawStringWithShadow(str, maxWidth-fontRenderer.getStringWidth(str)+3, y+2, Color.white.getRGB());
                }
                y+=12;
            }
        } else if (hasAnyUnseen()) {
            RenderUtils.drawRoundedGradientRect(3 + nameWidth, chat.height - 32, 10, 12, 5, 5, GRADIENT_1, GRADIENT_2);
            fontRenderer.drawStringWithShadow("!", 7 + nameWidth, chat.height - 30, Color.white.getRGB());
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        String chatName = mc.ingameGUI.currentChat.chatName();
        int nameWidth = fontRenderer.getStringWidth(chatName);
        boolean hovered = mouseX > 2 && mouseX < 4 + nameWidth && mouseY > chat.height - 28 && mouseY < chat.height - 16;
        if (hovered) {
            if (mouseButton == 0) {
                comboBox ^= true;
            }
        } else if (comboBox) {
            int y = chat.height - 28 - 12*mc.ingameGUI.chats().size();
            for (int i = 0; i < mc.ingameGUI.chats().size(); i++) {
                int maxWidth = maxWidth()+35;
                if (mouseX > 2 && mouseX < 4 + maxWidth && mouseY > y && mouseY < y+12) {
                    if (mouseButton == 0) {
                        mc.ingameGUI.currentChat = mc.ingameGUI.chats().get(i);
                        mc.ingameGUI.currentChat.messagesUnseen = 0;
                        comboBox = false;
                    } else if (mouseButton == 1) {
                        if (mc.ingameGUI.currentChat != mc.ingameGUI.getChatGUI() && mc.ingameGUI.currentChat != Ipana.mainIRC().ircChat()) {
                            IRC irc = Ipana.connectedIRCs().get(i-1);
                            irc.leave();
                            Ipana.connectedIRCs().remove(i-1);
                            mc.ingameGUI.chats().remove(i);
                            i--;
                            mc.ingameGUI.currentChat = mc.ingameGUI.chats().get(MathHelper.clamp_int(i, 0, mc.ingameGUI.chats().size()-1));
                        }
                    }
                }
                y+=12;
            }
        }
    }
    private boolean hasAnyUnseen() {
        for (GuiNewChat guiNewChat : mc.ingameGUI.chats()) {
            if (guiNewChat.messagesUnseen > 0) {
                return true;
            }
        }
        return false;
    }
    private int maxWidth() {
        int x = 0;
        for (GuiNewChat newChat : mc.ingameGUI.chats()) {
            int nameWidth = fontRenderer.getStringWidth(newChat.chatName());
            if (nameWidth > x) {
                x = nameWidth;
            }
        }
        return x;
    }
}
