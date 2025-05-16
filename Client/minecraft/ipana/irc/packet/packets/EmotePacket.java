package ipana.irc.packet.packets;

import ipana.irc.packet.Packet;
import ipana.irc.user.User;
import ipana.irc.user.UserProperties;
import ipana.utils.player.PlayerUtils;
import ipana.utils.render.Anims;
import ipana.utils.render.EmoteUtils;
import ipana.utils.render.PlayerAnim;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class EmotePacket extends Packet {


    @Override
    public String name() {
        return "emote";
    }

    @Override
    public void execute(User sender, String... args) {
        String emoteName = args[0];
        EmoteUtils.Emote emote = EmoteUtils.getEmote(emoteName);
        if (Minecraft.getMinecraft().theWorld != null && emote != null) {
            EntityPlayer player = PlayerUtils.getPlayer(sender.getProperty(UserProperties.INGAME_NAME));
            if (player != null) {
                player.activeEmotes().add(new EmoteUtils.Action(emote));
                if (emoteName.equals("nah")) {
                    Anims.add(player, new PlayerAnim(":nah:", System.currentTimeMillis(), player));
                }
            }
        }
    }

    @Override
    public boolean hasArgs() {
        return true;
    }
}
