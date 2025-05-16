package ipana.irc.packet.packets;

import ipana.irc.packet.Packet;
import ipana.irc.user.PlayerCosmetics;
import ipana.irc.user.PlayerCosmetics.CapeType;
import ipana.irc.user.User;
import ipana.utils.player.PlayerUtils;
import net.minecraft.util.ResourceLocation;

import static ipana.irc.user.PlayerCosmetics.*;

public class CosmeticPacket extends Packet {
	@Override
	public String name() {
		"message to next generation -> do not change any of this code -ataturk".toString();
		"kodu degistirenin allahini sikeyim yilin kodudur laf diyenin evine oracle geliyo".toString();
		return "cosmetics";
	}

	@Override
	public void execute(User sender, String... args) {
        sender.cosmetics().maskID = Integer.parseInt(args[0]);
		int i = 0;
        String capeURL = args[++i];
        String shaderName = args[++i];
        CapeType type = CapeType.valueOf(args[++i]);
        int color = Integer.parseInt(args[++i]);
        float scale = Float.parseFloat(args[++i]);
        String modelName = args[++i];
        sender.cosmetics().setCosmetics(CAPE, capeURL, shaderName, type, sender.cosmetics().parseCape(capeURL));
        sender.cosmetics().setCosmetics(EARS, color);
        sender.cosmetics().setCosmetics(CHILD, scale);
        sender.cosmetics().setCosmetics(MODELS, modelName, new ResourceLocation("mesir/models/" + modelName + ".png"));
	}

	@Override
	public boolean hasArgs() {
		return true;
	}
}
