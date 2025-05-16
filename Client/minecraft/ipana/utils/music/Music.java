package ipana.utils.music;

import ipana.utils.file.FileUtils;

import java.io.File;
import java.util.Locale;

public class Music {
    private File file;
    private String name;
    private MusicType type;
    private PlayLocation playLocation;


    public Music(String name, MusicType type, PlayLocation location) {
        this.name = name;
        this.type = type;
        this.playLocation = location;
        String locationType = location.name().toLowerCase(Locale.ENGLISH).replace("_","");
        String musicType = type.name().toLowerCase(Locale.ENGLISH);
        this.file = new File(FileUtils.getConfigDir(), "musics\\"+locationType+"\\"+name+"."+musicType);
        if (!file.exists()) {
            this.file = null;
        }
    }


    public File file() {
        return file;
    }

    public String name() {
        return name;
    }

    public MusicType type() {
        return type;
    }

    public PlayLocation playLocation() {
        return playLocation;
    }
}
