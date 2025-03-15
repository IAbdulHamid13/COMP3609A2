import java.awt.Image;

public class Animation {
    private final Image[] frames;
    private int currentFrame;
    private final boolean loop;
    private boolean active;

    public Animation(Image[] frames, boolean loop) {
        this.frames = frames;
        this.loop = loop;
        this.active = false;
    }

    public void update() {
        if (!active) return;
        currentFrame = (currentFrame + 1) % frames.length;
        if (!loop && currentFrame == frames.length - 1) {
            active = false;
        }
    }

    public Image getImage() {
        return frames[currentFrame];
    }

    public void start() {
        active = true;
        currentFrame = 0;
    }

    public boolean isActive() {
        return active;
    }
}
