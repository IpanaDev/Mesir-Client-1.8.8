package ipana.bot;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.IChatComponent;

public record LoginResponse(boolean success, GameProfile loginProfile, IChatComponent disconnectReason) {

}
