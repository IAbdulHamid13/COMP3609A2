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
    private boolean shieldActive = true;
    private long shieldEndTime = 0;

    // Entities
    private PlayerShip player;
    private List<Invader> invaders;
    private List<Bullet> bullets;
    private List<PowerUp> powerUps;
    private List<Star> stars;

    // Resources
    private BufferedImage playerImage;
    private BufferedImage[] invaderImages = new BufferedImage[2];
    private Clip shootSound, explosionSound, shieldSound, bgMusic, gameOverSound;

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
            // Load and resize invader images to match player ship size (40x30)
            BufferedImage originalInvaderImage = ImageIO
                    .read(getClass().getResourceAsStream("/Resources/Images/Invader.png"));
            invaderImages[0] = resizeImage(originalInvaderImage, 40, 30);
            invaderImages[1] = resizeImage(originalInvaderImage, 40, 30);

            // Load sounds
            shootSound = GameUtils.loadSound("/sounds/shoot.wav");
            explosionSound = GameUtils.loadSound("/sounds/explosion.wav");
            shieldSound = GameUtils.loadSound("/sounds/shield.wav");
            gameOverSound = GameUtils.loadSound("/sounds/game_over.wav");
            bgMusic = GameUtils.loadSound("/Resources/Sounds/LoopableBackgroundMusic.wav");
            if (bgMusic != null) {
                bgMusic.setFramePosition(0);
                FloatControl gainControl = (FloatControl) bgMusic.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(-10.0f); // Reduce volume by 10 decibels
                bgMusic.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (IOException e) {
            System.out.println("Error loading images: " + e.getMessage());
            e.printStackTrace(); // Add this to see detailed error message
            // Fallback to placeholder images if loading fails
            playerImage = createPlaceholderImage(40, 30, Color.GREEN);
            invaderImages[0] = createPlaceholderImage(40, 30, Color.RED);
            invaderImages[1] = createPlaceholderImage(40, 30, Color.ORANGE);
        } catch (Exception e) {
            System.out.println("Sound loading failed: " + e.getMessage());
        }
    }

    private BufferedImage createPlaceholderImage(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(0, 0, width - 1, height - 1);
        g2d.dispose();
        return image;
    }

    private void initGame() {
        player = new PlayerShip(playerImage, 400, 500, 5);
        invaders = new ArrayList<>();
        bullets = new ArrayList<>();
        powerUps = new ArrayList<>();
        stars = new ArrayList<>();
        for (int i = 0; i < STAR_COUNT; i++) {
            stars.add(new Star(getWidth()));
        }

        // Adjust spacing for invaders that are now 40x30
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

    private void updatePowerUps() {
        List<PowerUp> toRemove = new ArrayList<>();
        for (PowerUp pu : powerUps) {
            if (pu.getBounds().intersects(player.getBounds())) {
                activateShield();
                toRemove.add(pu);
            }
        }
        powerUps.removeAll(toRemove);
    }

    private void checkGameOver() {
        // Check for loss conditions
        for (Invader invader : invaders) {
            Rectangle bounds = invader.getBounds();
            if (bounds.y > 500) { // If invaders reach bottom
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
        // Check if shield has expired
        if (shieldActive && System.currentTimeMillis() > shieldEndTime) {
            shieldActive = false;
        }
        updateStars();
        updateInvaders();
        updateBullets();
        updatePowerUps();
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
        List<Invader> invadersToRemove = new ArrayList<>();

        for (Bullet bullet : bullets) {
            Rectangle bulletBounds = bullet.getBounds();

            if (bullet instanceof PlayerBullet) {
                // Player bullets can only hit invaders
                for (Invader invader : invaders) {
                    if (bulletBounds.intersects(invader.getBounds())) {
                        bulletsToRemove.add(bullet);
                        invadersToRemove.add(invader);
                        score += 100;
                        playSound(explosionSound);
                        break;
                    }
                }
            } else if (bullet instanceof InvaderBullet) {
                // Invader bullets can only hit the player
                if (!shieldActive && bulletBounds.intersects(player.getBounds())) {
                    bulletsToRemove.add(bullet);
                    lives--;
                    playSound(explosionSound);
                }
            }
        }

        bullets.removeAll(bulletsToRemove);
        invaders.removeAll(invadersToRemove);
    }

    private BufferedImage backBuffer;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backBuffer == null || backBuffer.getWidth() != getWidth() || backBuffer.getHeight() != getHeight()) {
            backBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D g2d = backBuffer.createGraphics();

        // Draw background gradient
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(0x0B, 0x00, 0x4E),
                0, getHeight(), new Color(0x16, 0x00, 0x85));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Draw stars
        for (Star star : stars) {
            star.draw(g2d);
        }

        // Draw existing game elements
        drawPlayer(g2d);
        drawInvaders(g2d);
        drawBullets(g2d);
        drawHUD(g2d);

        // Draw power-ups
        for (PowerUp pu : powerUps) {
            pu.draw(g2d);
        }

        g.drawImage(backBuffer, 0, 0, null);
        g2d.dispose();
    }

    private void drawPlayer(Graphics2D g2d) {
        player.draw(g2d);
        if (shieldActive) {
            Rectangle bounds = player.getBounds();
            g2d.setColor(new Color(0, 0, 255, 100));
            g2d.fillOval(bounds.x - 10, bounds.y - 10,
                    bounds.width + 20, bounds.height + 20);
        }
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

        if (shieldActive) {
            g2d.setColor(Color.BLUE);
            g2d.drawString("Shield Active!", 20, 60);
        }
    }

    private void activateShield() {
        shieldActive = true;
        shieldEndTime = System.currentTimeMillis() + 5000;
        playSound(shieldSound);
    }

    private void gameOver(boolean won) {
        isRunning = false;
        
        // Stop background music
        if (bgMusic != null) {
            bgMusic.stop();
        }
        
        if (won) {
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
        // Not needed
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not needed
    }
}