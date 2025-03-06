import javax.sound.sampled.*;

public class GameUtils {
    public static Clip loadSound(String path) {
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(
                    GameUtils.class.getResource(path)));
            return clip;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}