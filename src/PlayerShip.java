import java.awt.*;
import java.awt.image.BufferedImage;

public class PlayerShip {
    private BufferedImage image;
    private int x, y, speed;
    private long lastShotTime = 0;
    private static final long SHOT_COOLDOWN = 500; // 0.5 seconds in milliseconds

    public PlayerShip(BufferedImage image, int x, int y, int speed) {
        this.image = image;
        this.x = x;
        this.y = y;
        this.speed = speed;
        new Rectangle(x, y, image.getWidth(), image.getHeight());
    }

    public void moveLeft() {
        x = Math.max(30, x - speed);
    }

    public void moveRight() {
        x = Math.min(770 - image.getWidth(), x + speed);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, image.getWidth(), image.getHeight());
    }

    public void draw(Graphics2D g2d) {
        g2d.drawImage(image, x, y, null);
    }

    public boolean canShoot() {
        long currentTime = System.currentTimeMillis();
        return currentTime - lastShotTime >= SHOT_COOLDOWN;
    }

    public void updateLastShotTime() {
        lastShotTime = System.currentTimeMillis();
    }
}