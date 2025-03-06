import java.awt.*;

public class Star {
    private float x, y;
    private float speed;
    private float size;
    private float brightness;

    public Star(int screenWidth) {
        reset(screenWidth);
        y = (float) (Math.random() * 600); // Random initial y position
    }

    public void reset(int screenWidth) {
        x = (float) (Math.random() * screenWidth);
        y = 0;
        speed = 1 + (float) (Math.random() * 5);
        size = 1 + (float) (Math.random() * 2);
        brightness = 0.2f + (float) (Math.random() * 0.8f);
    }

    public void update() {
        y += speed;
    }

    public void draw(Graphics2D g2d) {
        int alpha = (int) (brightness * 255);
        g2d.setColor(new Color(255, 255, 255, alpha));
        g2d.fillOval((int) x, (int) y, (int) size, (int) size);
    }

    public boolean isOffscreen() {
        return y > 600;
    }
}