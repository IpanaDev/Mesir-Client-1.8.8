package ipana.modules.render;

import ipana.Ipana;
import ipana.events.EventPreUpdate;
import ipana.events.EventRender3D;
import ipana.events.EventTick;
import ipana.managements.module.Category;
import ipana.managements.module.Module;
import ipana.managements.value.Value;
import ipana.managements.value.values.BoolValue;
import ipana.managements.value.values.NumberValue;
import ipana.utils.music.Music;
import ipana.utils.music.MusicPlayer;
import ipana.utils.music.Musics;
import ipana.utils.music.PlayLocation;
import ipana.utils.player.PlayerUtils;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;

import pisi.unitedmeows.eventapi.event.listener.Listener;


public class Wide extends Module {

    public NumberValue<Integer> wide = new NumberValue<>("Wide",this,750,50,2000,50,":ADS:DASads");
    public BoolValue music = new BoolValue("Music", this, true, "Plays wide putin theme.");
    public BoolValue autoDisable = new BoolValue("AutoDisable", this, false, "Auto disables when music has stopped.");

    public Wide() {
        super("Wide", Keyboard.KEY_NONE,Category.Render,"wide putin lol");
    }

    @Override
    public void onEnable() {
        wideMultiplier = 0;
        if (music.getValue()) {
            Music wide = Musics.getInGame("Wide");
            if (wide == null) {
                PlayerUtils.debug("Couldn't find wide music.");
                super.onEnable();
                return;
            }
            if (MusicPlayer.isPlaying()) {
                MusicPlayer.stop();
            }
            MusicPlayer.play(wide, true);
        }
        super.onEnable();
    }
    public int wideMultiplier;


    private Listener<EventTick> onTick = new Listener<>(event -> {
        wideMultiplier++;
        if (music.getValue() && !MusicPlayer.isPlaying()) {
            Music wide = Musics.getInGame("Wide");
            if (wide != null) {
                if (!autoDisable.getValue()) {
                    wideMultiplier = 0;
                    MusicPlayer.play(wide, true);
                } else {
                    toggle();
                }
            }
        }
    });

    @Override
    public void onDisable() {
        wideMultiplier = 0;
        Music wide = Musics.getInGame("Wide");
        if (wide != null && MusicPlayer.isPlaying(wide)) {
            MusicPlayer.stop();
        }
        super.onDisable();
    }
}
