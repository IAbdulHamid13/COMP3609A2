import java.awt.*;

public abstract class Bullet {
    protected int x, y;
    protected static final int SPEED = 5;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void update();

    public Rectangle getBounds() {
        return new Rectangle(x, y, 5, 10);
    }

    public abstract void draw(Graphics2D g2d);
}