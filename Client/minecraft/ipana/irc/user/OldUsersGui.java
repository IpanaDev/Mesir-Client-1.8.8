package ipana.irc.user;

import ipana.irc.IRC;
import ipana.utils.font.FontHelper;
import ipana.utils.font.FontUtil;
import ipana.utils.render.RenderUtils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;

import javax.vecmath.Vector2d;
import java.awt.*;

@Deprecated
public class OldUsersGui extends GuiScreen {
    private final FontUtil font = FontHelper.SIZE_18;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        /*
        int x = 5;
        int y = 5;
        RenderUtils.drawFixedRect(0,0, RenderUtils.SCALED_RES.getScaledWidth(), RenderUtils.SCALED_RES.getScaledHeight(), Color.white.darker());
        User self = IRC.INSTANCE.self();
        font.drawStringWithShadow("Current User§6: §c"+self.senderName(), x, y, Color.darkGray.brighter());

        for (UserProperties property : UserProperties.VALUES) {
            y+=12;
            font.drawStringWithShadow(property.name()+"§6: §a" + self.getProperty(property), x, y, Color.darkGray.brighter());
        }
        GlStateManager.color(0,0,0);
        font.drawLine(new Vector2d(0, y+11), new Vector2d(345, y+11), 2);
        font.drawLine(new Vector2d(345, 0), new Vector2d(345, y+11), 2);
        y+=17;
        RenderUtils.drawFixedRect(x-1,y-2, x+63, y+9, Color.gray);
        font.drawStringWithShadow("§fOnline Users", x, y, Color.black);
        y+=12;
        for (User user : IRC.INSTANCE.users()) {
            String sender = user.senderName();
            String igName = user.getProperty(UserProperties.INGAME_NAME);
            String client = user.getProperty(UserProperties.CLIENT);
            String server = user.getProperty(UserProperties.SERVER);
            String text1 = "§c"+sender+"§f logged as §a"+igName;
            String text2 = "§fClient§6: §a"+client+"§f in §6"+server;
            float width = Math.max(font.getWidth(text1), FontHelper.SIZE_18.getWidth(text2));
            RenderUtils.drawFixedRect(x-1,y-1, x+width, y+17, Color.gray);
            font.drawStringWithShadow(text1, x, y, Color.white);
            FontHelper.SIZE_12.drawStringWithShadow(text2, x, y+11, Color.white);
            y+=20;
        }

         */

    }
}
