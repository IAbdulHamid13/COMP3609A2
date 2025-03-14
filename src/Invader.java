import java.awt.*;
import java.awt.image.BufferedImage;

public class Invader {
    private BufferedImage[] images;
    private BufferedImage[] explosionFrames;
    private int x, y, direction = 1;
    private boolean useAltSprite = false;
    private static final int SPEED = 2;
    private boolean exploding = false;
    private int explosionFrame = 0;
    private static final int EXPLOSION_FRAME_DELAY = 3;
    private int frameCounter = 0;

    public Invader(BufferedImage[] images, int x, int y) {
        this.images = images;
        this.x = x;
        this.y = y;
        loadExplosionAnimation();
    }

    private void loadExplosionAnimation() {
        explosionFrames = new BufferedImage[9];
        for (int i = 0; i < explosionFrames.length; i++) {
            String imagePath = "/Resources/Images/Invader_Explosion.gif";
            BufferedImage frame = ImageManager.loadBufferedImage(imagePath);
            if (frame != null) {
                explosionFrames[i] = resizeImage(frame, 40, 30);
            } else {
                // Create fallback image if loading fails
                explosionFrames[i] = createPlaceholderExplosion(40, 30, i);
            }
        }
    }

    private BufferedImage createPlaceholderExplosion(int width, int height, int frame) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Create a simple expanding circle animation
        float alpha = 1.0f - (frame / 8.0f);
        int size = (int) (width * (frame + 1) / 8.0f);

        g2d.setColor(new Color(1.0f, 0.5f, 0.0f, alpha)); // Orange with fading alpha
        g2d.fillOval((width - size) / 2, (height - size) / 2, size, size);

        g2d.dispose();
        return image;
    }

    public void startExplosion() {
        exploding = true;
        explosionFrame = 0;
    }

    public boolean isExploding() {
        return exploding;
    }

    public boolean isExplosionComplete() {
        return exploding && explosionFrame >= explosionFrames.length;
    }

    public void update(int screenWidth) {
        if (exploding) {
            frameCounter++;
            if (frameCounter >= EXPLOSION_FRAME_DELAY) {
                explosionFrame++;
                frameCounter = 0;
            }
            return;
        }

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
        if (exploding && explosionFrame < explosionFrames.length) {
            g2d.drawImage(explosionFrames[explosionFrame], x, y, null);
        } else if (!exploding) {
            g2d.drawImage(images[useAltSprite ? 1 : 0], x, y, null);
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resizedImage;
    }
}