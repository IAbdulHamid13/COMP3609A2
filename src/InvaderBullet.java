import java.awt.*;

public class InvaderBullet extends Bullet {

    public InvaderBullet(int x, int y) {
        super(x, y);
    }

    @Override
    public void update() {
        y += SPEED; // Moves downward
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        g2d.fillRect(x, y, 5, 10);
    }
}
