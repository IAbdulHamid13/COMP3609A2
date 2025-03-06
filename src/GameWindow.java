import javax.swing.JFrame;

public class GameWindow extends JFrame {
    private GamePanel gamePanel;

    public GameWindow(String title, int width, int height) {
        super(title);
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        gamePanel = new GamePanel();
        add(gamePanel);
        setVisible(true);
    }

    public void startGame() {
        gamePanel.startGameThread();
    }
}