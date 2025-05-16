package ipana.utils.render;

import net.minecraft.entity.Entity;

import java.util.ArrayList;
import java.util.HashMap;

public class Anims {

    public static final HashMap<Entity, ArrayList<PlayerAnim>> LIST = new HashMap<>();

    public static void add(Entity player, PlayerAnim anim) {
        LIST.putIfAbsent(player, new ArrayList<>());
        LIST.get(player).add(anim);
    }

    public static PlayerAnim getAnimByName(Entity player, String name) {
        ArrayList<PlayerAnim> anims = LIST.get(player);
        if (anims != null) {
            for (int i = 0; i < anims.size(); i++) {
                PlayerAnim anim = anims.get(i);
                if (anim.getName().equals(name) && System.currentTimeMillis() - anim.getMs() >= 2500) {
                    anims.remove(i--);
                } else {
                    return anim;
                }
            }
        }
        return null;
    }
}
