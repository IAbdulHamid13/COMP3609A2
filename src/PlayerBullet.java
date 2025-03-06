import java.awt.*;

public class PlayerBullet extends Bullet {

    public PlayerBullet(int x, int y) {
        super(x, y);
    }

    @Override
    public void update() {
        y -= SPEED; // Moves upward
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(0x11, 0x80, 0x6B)); // #11806B
        g2d.fillRect(x, y, 5, 10);
    }
}
