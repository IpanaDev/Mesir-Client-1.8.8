package ipana.utils.music;

import ipana.utils.file.FileUtils;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class Musics {
    public static final ArrayList<Music> MAIN_MENU = new ArrayList<>();
    public static final ArrayList<Music> IN_GAME = new ArrayList<>();

    public static void init() {
        Minecraft.setStatus("Music Player");
        File musicsFolder = new File(FileUtils.getConfigDir(), "musics");
        if (!musicsFolder.exists()) {
            musicsFolder.mkdir();
        }
        File mainmenu = new File(musicsFolder, "mainmenu");
        if (!mainmenu.exists()) {
            mainmenu.mkdir();
        }
        File ingame = new File(musicsFolder, "ingame");
        if (!ingame.exists()) {
            ingame.mkdir();
        }
        File[] mainMenuMusics = mainmenu.listFiles();
        File[] ingameMusics = ingame.listFiles();
        if (mainMenuMusics != null) {
            for (File file : mainMenuMusics) {
                for (MusicType type : MusicType.VALUES) {
                    String typeString = "."+type.name().toLowerCase(Locale.ENGLISH);
                    if (file.getName().endsWith(typeString)) {
                        MAIN_MENU.add(new Music(file.getName().replace(typeString, ""), type, PlayLocation.MAIN_MENU));
                    }
                }
            }
        }
        if (ingameMusics != null) {
            for (File file : ingameMusics) {
                for (MusicType type : MusicType.VALUES) {
                    String typeString = "."+type.name().toLowerCase(Locale.ENGLISH);
                    if (file.getName().endsWith(typeString)) {
                        IN_GAME.add(new Music(file.getName().replace(typeString, ""), type, PlayLocation.IN_GAME));
                    }
                }
            }
        }
    }

    public static Music getInGame(String name) {
        for (Music music : IN_GAME) {
            if (music.name().equals(name)) {
                return music;
            }
        }
        return null;
    }
}
