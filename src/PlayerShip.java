import java.awt.*;
import java.awt.image.BufferedImage;

public class PlayerShip {
    private BufferedImage originalImage;
    private BufferedImage image;
    private int x, y, speed;
    private long lastShotTime = 0;
    private static final long SHOT_COOLDOWN = 500; // 0.5 seconds in milliseconds
    private int maxLives = 3;
    private int currentLives = 3;

    public PlayerShip(BufferedImage image, int x, int y, int speed) {
        this.originalImage = image;
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

    public void updateDamageEffect(int lives) {
        currentLives = lives;
        if (currentLives < maxLives) {
            // Calculate red tint based on remaining lives
            float damageFactor = 1.0f - ((float) currentLives / maxLives);

            // Create new image with red tint
            BufferedImage tintedImage = new BufferedImage(
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = tintedImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();

            // Apply red tint
            for (int i = 0; i < tintedImage.getWidth(); i++) {
                for (int j = 0; j < tintedImage.getHeight(); j++) {
                    int rgb = tintedImage.getRGB(i, j);
                    if ((rgb >> 24) != 0) { // If pixel is not transparent
                        Color color = new Color(rgb, true);
                        int red = color.getRed();
                        int green = color.getGreen();
                        int blue = color.getBlue();

                        // Increase red and decrease other channels
                        red = Math.min(255, (int) (red + (255 - red) * damageFactor));
                        green = Math.max(0, (int) (green * (1 - damageFactor)));
                        blue = Math.max(0, (int) (blue * (1 - damageFactor)));

                        tintedImage.setRGB(i, j, new Color(red, green, blue, color.getAlpha()).getRGB());
                    }
                }
            }

            image = tintedImage;
        } else {
            image = originalImage;
        }
    }
}