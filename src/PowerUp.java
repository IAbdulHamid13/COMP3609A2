import java.awt.*;

public class PowerUp {
    private int x, y;

    public PowerUp(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, 20, 20);
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLUE);
        g2d.fillRect(x, y, 20, 20);
    }
}