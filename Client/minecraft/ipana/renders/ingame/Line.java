package ipana.renders.ingame;

import ipana.Ipana;
import ipana.irc.user.User;
import ipana.irc.user.UserProperties;
import ipana.utils.Animate;
import ipana.utils.render.RenderUtils;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class Line extends Animate {
    String sender, rawMessage, message;
    int leftTicks;
    boolean sentBySelf;

    public Line(String sender, String rawMessage, String message, boolean sentBySelf) {
        super(RenderUtils.SCALED_RES.getScaledWidth(), 0);
        this.sender = sender;
        this.rawMessage = rawMessage;
        this.message = message;
        this.leftTicks = 50;
        this.sentBySelf = sentBySelf;
    }


    public static Line create(String sender, String message, User user, boolean sentBySelf) {
        if (user.irc() == Ipana.mainIRC()) {
            return new Line(sender, message, "§f[§b"+user.getProperty(UserProperties.CLIENT)+"§f] §a"+sender+"§f: "+message, sentBySelf);
        } else {
            return new Line(sender, message, "§a"+sender+"§f: "+message, sentBySelf);
        }
    }
    public static ChatComponentText create(String sender, String message, User user) {
        if (user.irc() == Ipana.mainIRC()) {
            return new ChatComponentText(EnumChatFormatting.WHITE+"["+EnumChatFormatting.AQUA+user.getProperty(UserProperties.CLIENT)+EnumChatFormatting.WHITE+"] "+EnumChatFormatting.GREEN+sender+EnumChatFormatting.WHITE+": "+message);
        } else {
            return new ChatComponentText(EnumChatFormatting.GREEN+sender+EnumChatFormatting.WHITE+": "+message);
        }
    }
}
