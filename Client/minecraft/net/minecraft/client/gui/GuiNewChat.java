package net.minecraft.client.gui;

import com.google.common.collect.Lists;

import java.awt.*;
import java.util.*;
import java.util.List;

import ipana.Ipana;
import ipana.modules.render.Hud;
import ipana.utils.StringUtil;
import ipana.utils.font.FontHelper;
import ipana.utils.gl.GLCall;
import ipana.utils.player.PlayerUtils;
import ipana.utils.render.Anims;
import ipana.utils.render.EmoteUtils;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class GuiNewChat extends Gui
{
    private final String chatName;
    private final Minecraft mc;
    private final List<String> sentMessages = Lists.<String>newArrayList();
    private final List<ChatLine> chatLines = Lists.<ChatLine>newArrayList();
    private final List<ChatLine> field_146253_i = Lists.<ChatLine>newArrayList();
    private int scrollPos;
    private boolean isScrolled;
    private int updateCounter;
    public int messagesUnseen;

    public GuiNewChat(String chatName, Minecraft mcIn) {
        this.chatName = chatName;
        this.mc = mcIn;
    }


    private Color color = new Color(100,100,100,100);
    private IChatComponent listeningComponent;
    private GuiTextField textField = new GuiTextField(31,Minecraft.getMinecraft().fontRendererObj,1,1,1,1);

    public void drawChat(int p_146230_1_, int mouseX, int mouseY) {
        updateCounter = p_146230_1_;
        messagesUnseen = 0;
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int i = this.getLineCount();
            boolean flag = false;
            int j = 0;
            int k = this.field_146253_i.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (k > 0) {
                if (this.getChatOpen()) {
                    flag = true;
                }

                float f1 = this.getChatScale();
                int l = MathHelper.ceiling_float_int((float) this.getChatWidth() / f1);
                GlStateManager.pushMatrix();
                boolean callFromInGame = mouseX == 0 && mouseY == 0;
                if (callFromInGame) {
                    GlStateManager.translate(2.0F, 20, 0.0F);
                } else {
                    GlStateManager.translate(2.0F, RenderUtils.SCALED_RES.getScaledHeight() - 30, 0.0F);
                }
                GlStateManager.scale(1f, 1f, 1.0F);

                for (int i1 = 0; i1 + this.scrollPos < this.field_146253_i.size() && i1 < i; ++i1) {
                    ChatLine chatline = this.field_146253_i.get(i1 + this.scrollPos);

                    if (chatline != null) {
                        int j1 = p_146230_1_ - chatline.getUpdatedCounter();

                        if (j1 < 200 || flag) {
                            double d0 = (double) j1 / 200.0D;
                            d0 = 1.0D - d0;
                            d0 = d0 * 10.0D;
                            d0 = MathHelper.clamp_double(d0, 0.0D, 1.0D);
                            d0 = d0 * d0;
                            int l1 = (int) (255.0D * d0);

                            if (flag) {
                                l1 = 255;
                            }

                            l1 = (int) ((float) l1 * f);
                            ++j;
                            if (l1 > 3) {
                                int i2 = 0;
                                int j2 = (-i1 * 9);
                                int mX = (int) (mouseX / f1);
                                //PlayerUtils.debug(mY+ " : "+(RenderUtils.SCALED_RES.getScaledHeight()-i1*8));

                                if (!callFromInGame && mc.currentScreen instanceof GuiChat chat && !chat.chatSelection.comboBox && mX >= i2 && mX <= i2 + l + 4 && ((mouseY)) > (RenderUtils.SCALED_RES.getScaledHeight()+j2-9-30) && ((mouseY)) <= (RenderUtils.SCALED_RES.getScaledHeight()+j2 - 30)) {
                                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                                    drawRectNoStates(i2, j2 - 9, i2 + l + 4, j2, color.getRGB());
                                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                                }
                                if (!callFromInGame && chatline.getChatComponent() == listeningComponent) {
                                    String s = textField.getText();
                                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                                    drawRectNoStates(i2, j2 - 9, i2 + l + 4, j2, new Color(Ipana.getClientColor().getRed(), Ipana.getClientColor().getGreen(), Ipana.getClientColor().getBlue(), 100).getRGB());
                                    drawRectNoStates(i2 + l + 7, j2 - 9, i2 + l + 7 + mc.fontRendererObj.getStringWidth(s), j2, l1 / 2 << 24);
                                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                                    mc.fontRendererObj.drawStringWithShadow(s, (float) i2 + l + 7, (float) (j2 - 8), 16777215 + (l1 << 24));
                                    mc.fontRendererObj.drawStringWithShadow("_", mc.fontRendererObj.getStringWidth(s.substring(0, Math.min(s.length(), textField.getCursorPosition())))+l + 7, j2-8, Color.white.getRGB());
                                }
                                String s = chatline.getChatComponent().getFuckedText();
                                s = StringUtil.preventCrash(s);
                                HashMap<double[], EmoteUtils.Emote> emotes = new HashMap<>();
                                for (EmoteUtils.Emote emote : EmoteUtils.getList()) {
                                    String chatEmote = String.format("%s%s%s",":",emote.getName(),":");
                                    if (s.contains(chatEmote)) {
                                        int index = s.toLowerCase().indexOf(chatEmote);
                                        emotes.put(new double[]{i2 + mc.fontRendererObj.getStringWidth(s.substring(0, index)) - 1, j2 - 9}, emote);
                                        s = s.replace(chatEmote, "  ");
                                    }
                                }
                                int finalL = l1;
                                String finalS = s;
                                GlStateManager.translate(0, j2-8,0);
                                GLCall.draw(chatline.gList, s, c -> {
                                    GL11.glDisable(GL11.GL_TEXTURE_2D);
                                    drawRectNoStates(i2, -1, i2 + l + 4, 8, finalL / 2 << 24);
                                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                                    mc.fontRendererObj.drawStringWithShadow(finalS, (float) i2, 0, 16777215 + (finalL << 24));
                                });
                                GlStateManager.translate(0, -j2+8,0);
                                if (!emotes.isEmpty()) {
                                    GlStateManager.resetColor();
                                    GlStateManager.color(1,1,1,1);
                                    for (Map.Entry<double[], EmoteUtils.Emote> entry : emotes.entrySet()) {
                                        entry.getValue().render(entry.getKey()[0], entry.getKey()[1]);
                                    }
                                    emotes.clear();
                                }
                            }
                        }
                    }
                }

                if (flag) {
                    int k2 = this.mc.fontRendererObj.FONT_HEIGHT;
                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                    int l2 = k * k2 + k;
                    int i3 = j * k2 + j;
                    int j3 = this.scrollPos * i3 / k;
                    int k1 = i3 * i3 / l2;

                    if (l2 != i3) {
                        int k3 = j3 > 0 ? 170 : 96;
                        int l3 = this.isScrolled ? 13382451 : 3355562;
                        drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
                        drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
                    }
                }
                GlStateManager.popMatrix();
            }
        }
    }
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int i = this.getLineCount();
            boolean flag = false;
            int j = 0;
            int k = this.field_146253_i.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (k > 0) {
                if (this.getChatOpen()) {
                    flag = true;
                }

                float f1 = this.getChatScale();
                int l = MathHelper.ceiling_float_int((float)this.getChatWidth() / f1);
                for (int i1 = 0; i1 + this.scrollPos < this.field_146253_i.size() && i1 < i; ++i1) {
                    ChatLine chatline = this.field_146253_i.get(i1 + this.scrollPos);

                    if (chatline != null) {
                        int j1 = updateCounter - chatline.getUpdatedCounter();

                        if (j1 < 200 || flag) {
                            double d0 = (double)j1 / 200.0D;
                            d0 = 1.0D - d0;
                            d0 = d0 * 10.0D;
                            d0 = MathHelper.clamp_double(d0, 0.0D, 1.0D);
                            d0 = d0 * d0;
                            int l1 = (int)(255.0D * d0);

                            if (flag) {
                                l1 = 255;
                            }

                            l1 = (int)((float)l1 * f);
                            ++j;
                            if (l1 > 3) {
                                int i2 = 0;
                                int j2 = -i1 * 9;
                                int mX = (int) (mouseX/f1);
                                int mY =  ((mouseY));
                                //PlayerUtils.debug(mY+ " : "+(RenderUtils.SCALED_RES.getScaledHeight()-i1*8));
                                if (mX >= i2 && mX <= i2 + l + 4 && mY > RenderUtils.SCALED_RES.getScaledHeight()+j2-9 - 30 && mY <= RenderUtils.SCALED_RES.getScaledHeight()+j2 - 30) {
                                    if (mouseButton == 2) {
                                        chatline.getChatComponent().setText(chatline.getChatComponent().defaultText());
                                        listeningComponent = null;
                                    } else if (mouseButton == 1) {
                                        listeningComponent = chatline.getChatComponent();
                                        textField.setMaxStringLength(2173);
                                        textField.setText(chatline.getChatComponent().getFuckedText());
                                        textField.setFocused(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean keyTyped(char typedChar, int keyCode) {
        if (listeningComponent != null) {
            textField.textboxKeyTyped(typedChar, keyCode);
            if (keyCode == 56) {
                textField.setText(textField.getText()+"§");
            }
            if (keyCode == Keyboard.KEY_RETURN) {
                listeningComponent.setText(textField.getText());
                textField.setFocused(false);
                listeningComponent = null;
            }
            if (keyCode == Keyboard.KEY_ESCAPE) {
                textField.setFocused(false);
                listeningComponent = null;
            }
            return false;
        }
        return true;
    }
    /**
     * Clears the chat.
     */
    public void clearChatMessages() {
        for (ChatLine line : field_146253_i) {
            if (line != null) {
                line.gList.deleteList();
            }
        }
        field_146253_i.clear();
        this.chatLines.clear();
        this.sentMessages.clear();
    }

    public void printChatMessage(IChatComponent p_146227_1_)
    {
        this.printChatMessageWithOptionalDeletion(p_146227_1_, 0);
    }

    /**
     * prints the ChatComponent to Chat. If the ID is not 0, deletes an existing Chat Line of that ID from the GUI
     */
    public void printChatMessageWithOptionalDeletion(IChatComponent p_146234_1_, int p_146234_2_)
    {
        this.setChatLine(p_146234_1_, p_146234_2_, this.mc.ingameGUI.getUpdateCounter(), false);
        //logger.info("[CHAT] " + p_146234_1_.getUnformattedText());
    }

    private void setChatLine(IChatComponent p_146237_1_, int p_146237_2_, int p_146237_3_, boolean p_146237_4_)
    {
        System.out.println((p_146237_1_.getUnformattedText()));
        if (p_146237_2_ != 0)
        {
            this.deleteChatLine(p_146237_2_);
        }

        int i = MathHelper.floor_float((float)this.getChatWidth() / this.getChatScale());
        List<IChatComponent> list = GuiUtilRenderComponents.func_178908_a(p_146237_1_, i, this.mc.fontRendererObj, false, false);
        boolean flag = this.getChatOpen();

        for (IChatComponent ichatcomponent : list)
        {
            if (flag && this.scrollPos > 0)
            {
                this.isScrolled = true;
                this.scroll(1);
            }

            this.field_146253_i.add(0, new ChatLine(p_146237_3_, ichatcomponent, p_146237_2_));
        }



        if (!p_146237_4_)
        {
            this.chatLines.add(0, new ChatLine(p_146237_3_, p_146237_1_, p_146237_2_));


        }
    }

    public void refreshChat()
    {
        this.field_146253_i.clear();
        this.resetScroll();

        for (int i = this.chatLines.size() - 1; i >= 0; --i)
        {
            ChatLine chatline = (ChatLine)this.chatLines.get(i);
            this.setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
        }
    }

    public List<String> getSentMessages()
    {
        return this.sentMessages;
    }

    /**
     * Adds this string to the list of sent messages, for recall using the up/down arrow keys
     */
    public void addToSentMessages(String p_146239_1_)
    {
        if (this.sentMessages.isEmpty() || !((String)this.sentMessages.get(this.sentMessages.size() - 1)).equals(p_146239_1_))
        {
            this.sentMessages.add(p_146239_1_);
        }
    }

    public String chatName() {
        return chatName;
    }

    /**
     * Resets the chat scroll (executed when the GUI is closed, among others)
     */
    public void resetScroll()
    {
        this.scrollPos = 0;
        this.isScrolled = false;
    }

    /**
     * Scrolls the chat by the given number of lines.
     */
    public void scroll(int p_146229_1_)
    {
        this.scrollPos += p_146229_1_;
        int i = this.field_146253_i.size();

        if (this.scrollPos > i - this.getLineCount())
        {
            this.scrollPos = i - this.getLineCount();
        }

        if (this.scrollPos <= 0)
        {
            this.scrollPos = 0;
            this.isScrolled = false;
        }
    }

    /**
     * Gets the chat component under the mouse
     */
    public IChatComponent getChatComponent(int p_146236_1_, int p_146236_2_)
    {
        if (!this.getChatOpen())
        {
            return null;
        }
        else
        {
            ScaledResolution scaledresolution = RenderUtils.SCALED_RES;
            int i = scaledresolution.getScaleFactor();
            float f = this.getChatScale();
            int j = p_146236_1_ / i - 3;
            int k = p_146236_2_ / i - 27;
            j = MathHelper.floor_float((float)j / f);
            k = MathHelper.floor_float((float)k / f);

            if (j >= 0 && k >= 0)
            {
                int l = Math.min(this.getLineCount(), this.field_146253_i.size());

                if (j <= MathHelper.floor_float((float)this.getChatWidth() / this.getChatScale()) && k < this.mc.fontRendererObj.FONT_HEIGHT * l + l)
                {
                    int i1 = k / this.mc.fontRendererObj.FONT_HEIGHT + this.scrollPos;

                    if (i1 >= 0 && i1 < this.field_146253_i.size())
                    {
                        ChatLine chatline = this.field_146253_i.get(i1);
                        int j1 = 0;
                        if (chatline != null) {
                            for (IChatComponent ichatcomponent : chatline.getChatComponent()) {
                                if (ichatcomponent instanceof ChatComponentText)
                                {
                                    j1 += this.mc.fontRendererObj.getStringWidth(GuiUtilRenderComponents.func_178909_a(((ChatComponentText)ichatcomponent).getChatComponentText_TextValue(), false));

                                    if (j1 > j)
                                    {
                                        return ichatcomponent;
                                    }
                                }
                            }
                        }
                    }

                    return null;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * Returns true if the chat GUI is open
     */
    public boolean getChatOpen()
    {
        return this.mc.currentScreen instanceof GuiChat;
    }

    /**
     * finds and deletes a Chat line by ID
     */
    public void deleteChatLine(int p_146242_1_)
    {
        Iterator<ChatLine> iterator = this.field_146253_i.iterator();

        while (iterator.hasNext())
        {
            ChatLine chatline = (ChatLine)iterator.next();
            if (chatline != null) {
                if (chatline.getChatLineID() == p_146242_1_) {
                    iterator.remove();
                }
            }
        }

        iterator = this.chatLines.iterator();

        while (iterator.hasNext())
        {
            ChatLine chatline1 = (ChatLine)iterator.next();

            if (chatline1.getChatLineID() == p_146242_1_)
            {
                iterator.remove();
                break;
            }
        }
    }

    public int getChatWidth()
    {
        return calculateChatboxWidth(this.mc.gameSettings.chatWidth);
    }

    public int getChatHeight()
    {
        return calculateChatboxHeight(this.getChatOpen() ? this.mc.gameSettings.chatHeightFocused : this.mc.gameSettings.chatHeightUnfocused);
    }

    /**
     * Returns the chatscale from mc.gameSettings.chatScale
     */
    public float getChatScale()
    {
        return 1;
        //malım
        //return this.mc.gameSettings.chatScale;
    }

    public static int calculateChatboxWidth(float p_146233_0_)
    {
        int i = 360;
        int j = 40;
        return MathHelper.floor_float(p_146233_0_ * (float)(i - j) + (float)j);
    }

    public static int calculateChatboxHeight(float p_146243_0_)
    {
        int i = 180;
        int j = 20;
        return MathHelper.floor_float(p_146243_0_ * (float)(i - j) + (float)j);
    }

    public int getLineCount()
    {
        return this.getChatHeight() / 9;
    }

}
