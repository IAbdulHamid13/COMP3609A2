import java.awt.*;
import java.awt.image.BufferedImage;

public class Invader {
    private BufferedImage[] images;
    private int x, y, direction = 1;
    private boolean useAltSprite = false;
    private static final int SPEED = 2;

    public Invader(BufferedImage[] images, int x, int y) {
        this.images = images;
        this.x = x;
        this.y = y;
    }

    public void update(int screenWidth) {
        x += direction * SPEED;
        if (x > screenWidth - 50 || x < 0) {
            direction *= -1;
            y += 30;
        }
        useAltSprite = !useAltSprite;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, images[0].getWidth(), images[0].getHeight());
    }

    public void draw(Graphics2D g2d) {
        g2d.drawImage(images[useAltSprite ? 1 : 0], x, y, null);
    }
}