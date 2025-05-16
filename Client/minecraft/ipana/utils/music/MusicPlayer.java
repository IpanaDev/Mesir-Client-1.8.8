package ipana.utils.music;

import javax.sound.sampled.*;
import java.io.IOException;

public class MusicPlayer {
    //Queue can be added
    private static AudioInputStream inputStream;
    private static Clip clip;
    private static Music lastPlayed;

    public static void play(Music music) {
        play(music, false);
    }

    public static void play(Music music, boolean force) {
        try {
            if (inputStream == null || force || lastPlayed != music) {
                inputStream = AudioSystem.getAudioInputStream(music.file());
                clip = AudioSystem.getClip();
                clip.open(inputStream);
                clip.start();
                lastPlayed = music;
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (inputStream != null && clip != null) {
            clip.stop();
        }
    }

    public static Music lastPlayed() {
        return lastPlayed;
    }

    public static boolean isPlaying(Music music) {
        return clip != null && lastPlayed == music && clip.isActive();
    }

    public static boolean isPlaying() {
        return clip != null && clip.isActive();
    }
}
