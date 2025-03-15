import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    // Game constants
    private static final int FPS = 30;
    private static final int STAR_COUNT = 200;

    // Game state
    private Thread gameThread;
    private boolean isRunning = true;
    private int score = 0;
    private int lives = 3;

    // Entities
    private PlayerShip player;
    private List<Invader> invaders;
    private List<Bullet> bullets;
    private List<Star> stars;

    // Resources
    private BufferedImage playerImage;
    private BufferedImage[] invaderImages = new BufferedImage[2];
    private Clip shootSound, explosionSound, bgMusic, gameOverSound;
    private Clip invaderDieSound;
    private Clip playerDieSound;
    private Clip victorySound;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(new Color(0x0B, 0x00, 0x4E)); // #0B004E
        addKeyListener(this);
        setFocusable(true);
        initResources();
        initGame();
    }

    private void initResources() {
        try {
            // Load and resize player image to 40x30
            playerImage = ImageIO.read(getClass().getResourceAsStream("/Resources/Images/PlayerShip.png"));
            playerImage = resizeImage(playerImage, 40, 30);
            BufferedImage originalInvaderImage = ImageIO
                    .read(getClass().getResourceAsStream("/Resources/Images/Invader.png"));
            invaderImages[0] = resizeImage(originalInvaderImage, 40, 30);
            invaderImages[1] = resizeImage(originalInvaderImage, 40, 30);

            // Load sounds
            shootSound = GameUtils.loadSound("/Resources/Sounds/PlayerShip_ShotSound.wav");
            explosionSound = GameUtils.loadSound("/sounds/explosion.wav");
            gameOverSound = GameUtils.loadSound("/sounds/game_over.wav");
            bgMusic = GameUtils.loadSound("/Resources/Sounds/LoopableBackgroundMusic.wav");
            invaderDieSound = GameUtils.loadSound("/Resources/Sounds/Invader_Die_Sound_Effect.wav");
            playerDieSound = GameUtils.loadSound("/Resources/Sounds/PlayerShip_Die_Sound_Effect.wav");
            victorySound = GameUtils.loadSound("/Resources/Sounds/Victory_Theme.wav");
            if (bgMusic != null) {
                bgMusic.setFramePosition(0);
                FloatControl gainControl = (FloatControl) bgMusic.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(-10.0f); // Reduce volume by 10 decibels
                bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (IOException e) {
            System.err.println("Resource loading failed");
            createFallbackResources();
        } catch (Exception e) {
            System.out.println("Sound loading failed, now you can't hear the amazing music :(");
        }
    }

    private void createFallbackResources() {
        playerImage = new BufferedImage(40, 30, BufferedImage.TYPE_INT_ARGB);
        invaderImages[0] = playerImage;
        invaderImages[1] = playerImage;
    }

    private void initGame() {
        player = new PlayerShip(playerImage, 400, 500, 5);
        player.updateDamageEffect(lives); // Set initial state
        invaders = new ArrayList<>();
        bullets = new ArrayList<>();
        stars = new ArrayList<>();
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star(getWidth()));
        }

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 8; col++) {
                invaders.add(new Invader(invaderImages, 100 + col * 60, 100 + row * 50));
            }
        }
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (isRunning) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    private void updateBullets() {
        List<Bullet> toRemove = new ArrayList<>();
        for (Bullet bullet : bullets) {
            bullet.update();
            Rectangle bounds = bullet.getBounds();
            if (bounds.y < 0 || bounds.y > getHeight()) {
                toRemove.add(bullet);
            }
        }
        bullets.removeAll(toRemove);
    }

    private void checkGameOver() {
        // Check for loss conditions
        for (Invader invader : invaders) {
            Rectangle bounds = invader.getBounds();
            if (bounds.y > 500) { // If invaders reach bottom (untested)
                gameOver(false);
                return;
            }
        }
        if (lives <= 0) {
            gameOver(false);
            return;
        }

        // Check for win condition
        if (invaders.isEmpty()) {
            gameOver(true);
        }
    }

    private void update() {
        updateInvaders();
        updateBullets();
        updateStars();
        checkCollisions();
        checkGameOver();
    }

    private void updateInvaders() {
        for (Invader invader : invaders) {
            invader.update(getWidth());
            if (Math.random() < 0.001) {
                Rectangle bounds = invader.getBounds();
                bullets.add(new InvaderBullet(bounds.x + bounds.width / 2, bounds.y + bounds.height));
            }
        }
    }

    private void updateStars() {
        for (Star star : stars) {
            star.update();
            if (star.isOffscreen()) {
                star.reset(getWidth());
            }
        }
    }

    private void checkCollisions() {
        List<Bullet> bulletsToRemove = new ArrayList<>();

        for (Bullet bullet : bullets) {
            Rectangle bulletBounds = bullet.getBounds();

            if (bullet instanceof PlayerBullet) {
                for (Invader invader : invaders) {
                    if (!invader.isExploding() && bulletBounds.intersects(invader.getBounds())) {
                        bulletsToRemove.add(bullet);
                        invader.startExplosion();
                        playSound(invaderDieSound);
                        score += 100;
                        break;
                    }
                }
            } else if (bullet instanceof InvaderBullet) {
                if (bulletBounds.intersects(player.getBounds())) {
                    bulletsToRemove.add(bullet);
                    lives--;
                    player.updateDamageEffect(lives);
                    playSound(playerDieSound);
                }
            }
        }

        invaders.removeIf(Invader::isExplosionComplete);
        bullets.removeAll(bulletsToRemove);
    }

    private BufferedImage backBuffer;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backBuffer == null || backBuffer.getWidth() != getWidth() || backBuffer.getHeight() != getHeight()) {
            backBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D g2d = backBuffer.createGraphics();

        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(0x0B, 0x00, 0x4E),
                0, getHeight(), new Color(0x16, 0x00, 0x85));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        for (Star star : stars) {
            star.draw(g2d);
        }

        drawPlayer(g2d);
        drawInvaders(g2d);
        drawBullets(g2d);
        drawHUD(g2d);
        g.drawImage(backBuffer, 0, 0, null);
        g2d.dispose();
    }

    private void drawPlayer(Graphics2D g2d) {
        player.draw(g2d);
    }

    private void drawInvaders(Graphics2D g2d) {
        for (Invader invader : invaders) {
            invader.draw(g2d);
        }
    }

    private void drawBullets(Graphics2D g2d) {
        for (Bullet bullet : bullets) {
            bullet.draw(g2d);
        }
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.drawString("Score: " + score, 20, 20);
        g2d.drawString("Lives: " + lives, 20, 40);
    }

    private void gameOver(boolean won) {
        isRunning = false;

        if (bgMusic != null) {
            bgMusic.stop();
        }

        if (won) {
            playSound(victorySound);
            JOptionPane.showMessageDialog(this,
                    "Congratulations! You Won!\nFinal Score: " + score,
                    "Victory!",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            playSound(gameOverSound);
            JOptionPane.showMessageDialog(this,
                    "Game Over!\nFinal Score: " + score,
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT)
            player.moveLeft();
        if (key == KeyEvent.VK_RIGHT)
            player.moveRight();
        if (key == KeyEvent.VK_SPACE)
            fireBullet();
    }

    private void fireBullet() {
        if (player.canShoot()) {
            Rectangle bounds = player.getBounds();
            bullets.add(new PlayerBullet(bounds.x + bounds.width / 2, bounds.y));
            playSound(shootSound);
            player.updateLastShotTime();
        }
    }

    private void playSound(Clip sound) {
        if (sound != null) {
            sound.setFramePosition(0);
            sound.start();
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

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}