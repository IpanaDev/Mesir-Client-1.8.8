package ipana.bot;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

public class BotWrapper {

    public static GameProfile fromName(String name) {
        return new GameProfile(UUID.randomUUID(), name);
    }
}
