import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class GameMap3 extends JPanel implements KeyListener {
    private final int TILE_SIZE = 32;
    private final int TILES_PER_ROW = 74;
    private Image tileset;

    private final JFrame parentFrame;
    private int playerX = 6, playerY = 2;
    private int offsetX = 0, offsetY = 0;
    private boolean isAnimating = false;

    private final int RETURN_TRIGGER_X = 6;
    private final int RETURN_TRIGGER_Y = 2;
    private boolean showReturnMessage = false;

    private PauseMenuPanel pauseMenu;
    private boolean menuVisible = false;

    private int[][] layer1;

    private int cameraX = 0;
    private int cameraY = 0;

    private static final int STATE_IDLE = 0;
    private static final int STATE_RUN = 1;
    private int state = STATE_IDLE;

    private Image[] idleFrames;
    private Image[] runFrames;
    private int currentFrame = 0;
    private Timer animationTimer;
    private final int FRAME_WIDTH = 200;
    private final int FRAME_HEIGHT = 200;
    private final int IDLE_FRAMES = 4;
    private final int RUN_FRAMES = 8;

    private Rectangle hitbox;
    private final int HITBOX_WIDTH = 24;
    private final int HITBOX_HEIGHT = 32;
    private final int HITBOX_OFFSET_X = 4;
    private final int HITBOX_OFFSET_Y = -2;

    private final int LEVEL3_TRIGGER_X = 16;
    private final int LEVEL3_TRIGGER_Y = 9;
    private boolean showLevel3Message = false;

    private boolean wPressed, aPressed, sPressed, dPressed;
    private boolean facingRight = true;

    private static final int STATE_ATTACK = 2;
    private Image[] attackFrames;
    private final int ATTACK_FRAMES = 4;

    private Rectangle attackHitbox = new Rectangle();
    private final int ATTACK_HITBOX_WIDTH = 40;
    private final int ATTACK_HITBOX_HEIGHT = 24;
    private final int ATTACK_HITBOX_OFFSET_Y = 10;
    private final int ATTACK_HITBOX_RIGHT_OFFSET = 24;

    /// Enemy
    private List<Enemy> enemies = new ArrayList<>();

    public GameMap3(JFrame parentFrame) {
        this.parentFrame = parentFrame;

        tileset = new ImageIcon("assets/tiles/tileset x2.png").getImage();
        layer1 = loadCSV("assets/maps/nivel3.csv");

        loadPlayerSprites();
        startAnimation();

        enemies.add(new Enemy(5, 5));
        enemies.add(new Enemy(10, 8));

        hitbox = new Rectangle(
                playerX * TILE_SIZE + HITBOX_OFFSET_X,
                playerY * TILE_SIZE + HITBOX_OFFSET_Y,
                HITBOX_WIDTH,
                HITBOX_HEIGHT
        );

        setLayout(null);
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    currentFrame = 0;
                    state = STATE_ATTACK;
                }
            }
        });

        pauseMenu = new PauseMenuPanel(
                () -> { parentFrame.dispose(); new MainMenu(); },
                () -> { JOptionPane.showMessageDialog(this, "Load not implemented yet."); },
                () -> { JOptionPane.showMessageDialog(this, "Options coming soon!"); },
                () -> { System.exit(0); }
        );
        pauseMenu.setBounds(0, 0, getWidth(), getHeight());
        pauseMenu.setVisible(false);
        add(pauseMenu);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                pauseMenu.setBounds(0, 0, getWidth(), getHeight());
            }
        });
    }

    private void loadPlayerSprites() {
        try {
            BufferedImage idleSheet = javax.imageio.ImageIO.read(new File("assets/sprites/NyxSprites/Idle.png"));
            BufferedImage runSheet = javax.imageio.ImageIO.read(new File("assets/sprites/NyxSprites/Run.png"));
            BufferedImage attackSheet = ImageIO.read(new File("assets/sprites/NyxSprites/Attack2.png"));

            idleFrames = new Image[IDLE_FRAMES];
            runFrames = new Image[RUN_FRAMES];
            attackFrames = new Image[ATTACK_FRAMES];

            for (int i = 0; i < IDLE_FRAMES; i++) {
                BufferedImage frame = idleSheet.getSubimage(i * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT);
                idleFrames[i] = frame.getScaledInstance(150, 140, Image.SCALE_SMOOTH);
            }

            for (int i = 0; i < RUN_FRAMES; i++) {
                BufferedImage frame = runSheet.getSubimage(i * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT);
                runFrames[i] = frame.getScaledInstance(150, 140, Image.SCALE_SMOOTH);
            }

            for (int i = 0; i < ATTACK_FRAMES; i++) {
                BufferedImage frame = attackSheet.getSubimage(i * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT);
                attackFrames[i] = frame.getScaledInstance(150, 140, Image.SCALE_SMOOTH);
            }

        } catch (Exception e) {
            System.out.println("Eroare la încărcarea sprite-urilor ninja:");
            e.printStackTrace();
        }
    }

    private void startAnimation() {
        animationTimer = new Timer(120, e -> {
            currentFrame++;
            switch (state) {
                case STATE_IDLE:
                    currentFrame %= IDLE_FRAMES;
                    break;
                case STATE_RUN:
                    currentFrame %= RUN_FRAMES;
                    break;
                case STATE_ATTACK: {
                    int attackX = facingRight
                            ? playerX * TILE_SIZE + ATTACK_HITBOX_RIGHT_OFFSET
                            : playerX * TILE_SIZE - ATTACK_HITBOX_WIDTH;

                    attackHitbox.setBounds(
                            attackX,
                            playerY * TILE_SIZE + ATTACK_HITBOX_OFFSET_Y,
                            ATTACK_HITBOX_WIDTH,
                            ATTACK_HITBOX_HEIGHT
                    );

                    for (Enemy enemy : enemies) {
                        if (!enemy.isDead()
                                && playerY == enemy.getY()
                                && attackHitbox.intersects(enemy.getHitbox())) {
                            enemy.die();
                            System.out.println("Inamic lovit!");
                        }
                    }

                    if (currentFrame >= ATTACK_FRAMES) {
                        currentFrame = 0;
                        state = (wPressed || aPressed || sPressed || dPressed) ? STATE_RUN : STATE_IDLE;
                    }
                    break;
                }
            }
            repaint();
        });

        animationTimer.start();

        for (Enemy enemy : enemies) {
            enemy.updateAnimation();
        }
    }

    private Image flipImageHorizontally(Image img) {
        BufferedImage original = new BufferedImage(
                img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g = original.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-original.getWidth(), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

        return op.filter(original, null);
    }

    private int[][] loadCSV(String path) {
        List<int[]> rows = new ArrayList<>();
        try (Scanner scanner = new Scanner(new File(path))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] tokens = line.split(",");
                int[] row = new int[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    row[i] = Integer.parseInt(tokens[i].trim());
                }
                rows.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rows.toArray(new int[0][0]);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        updateCamera();

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(-cameraX, -cameraY);

        drawLayer(g2d, layer1);

        int spriteWidth = 150;
        int spriteHeight = 150;

        int drawX = playerX * TILE_SIZE + offsetX + (TILE_SIZE - spriteWidth) / 2;
        int drawY = playerY * TILE_SIZE + offsetY + (TILE_SIZE - spriteHeight) / 2;

        Image sprite = null;
        switch (state) {
            case STATE_IDLE -> sprite = idleFrames[currentFrame % IDLE_FRAMES];
            case STATE_RUN -> sprite = runFrames[currentFrame % RUN_FRAMES];
            case STATE_ATTACK -> sprite = attackFrames[Math.min(currentFrame, ATTACK_FRAMES - 1)];
        }


        if (sprite != null) {
            if (!facingRight) sprite = flipImageHorizontally(sprite);
            g2d.drawImage(sprite, drawX, drawY, this);
        } else {
            g2d.setColor(Color.RED);
            g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
        }

        g2d.setColor(Color.GREEN);
        g2d.draw(hitbox);

        for (Enemy enemy : enemies) {
            enemy.updateAnimation();
            enemy.updateHitbox(TILE_SIZE);
            enemy.draw((Graphics2D) g2d, TILE_SIZE);
        }

        for (Enemy enemy : enemies) {
            enemy.updateHitbox(TILE_SIZE); // redundant, dar sigur
            if (hitbox.intersects(enemy.getHitbox())) {
                System.out.println("Coliziune cu inamicul!");
                // aici poți pune logică de damage, eliminare, etc.
            }
        }

        if (showReturnMessage) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("Press E to return to the main map", cameraX + 50, cameraY + 50);
        }


        if (showLevel3Message) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("Press 3 if you want to enter level 3", cameraX + 50, cameraY + 50);
        }
        g2d.dispose();
    }

    private void drawLayer(Graphics g, int[][] layer) {
        for (int row = 0; row < layer.length; row++) {
            for (int col = 0; col < layer[0].length; col++) {
                int tileId = layer[row][col];
                if (tileId != 0) {
                    int tileCol = tileId % TILES_PER_ROW;
                    int tileRow = tileId / TILES_PER_ROW;
                    g.drawImage(tileset,
                            col * TILE_SIZE, row * TILE_SIZE, (col + 1) * TILE_SIZE, (row + 1) * TILE_SIZE,
                            tileCol * TILE_SIZE, tileRow * TILE_SIZE, (tileCol + 1) * TILE_SIZE, (tileRow + 1) * TILE_SIZE,
                            this);
                }
            }
        }
    }

    private void updateCamera() {
        int mapWidth = layer1[0].length * TILE_SIZE;
        int mapHeight = layer1.length * TILE_SIZE;

        int viewportWidth = parentFrame.getContentPane().getWidth();
        int viewportHeight = parentFrame.getContentPane().getHeight();

        cameraX = playerX * TILE_SIZE + offsetX - viewportWidth / 2 + TILE_SIZE / 2;
        cameraY = playerY * TILE_SIZE + offsetY - viewportHeight / 2 + TILE_SIZE / 2;

        cameraX = Math.max(0, Math.min(cameraX, mapWidth - viewportWidth));
        cameraY = Math.max(0, Math.min(cameraY, mapHeight - viewportHeight));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(layer1[0].length * TILE_SIZE, layer1.length * TILE_SIZE);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
        {
            menuVisible = !menuVisible;
            pauseMenu.setVisible(menuVisible);

            if (menuVisible)
            {
                pauseMenu.requestFocusInWindow();
            }
            else
            {
                requestFocusInWindow();
            }

            repaint();
            return;
        }

        if (e.getKeyChar() == 'e' && showLevel3Message) {
            JOptionPane.showMessageDialog(this, "Intrăm în nivelul 3!");
            return;
        }

        if (isAnimating || menuVisible) return;
        if (state == STATE_ATTACK) return;

        final int[] dx = {0}, dy = {0};

        switch (e.getKeyChar()) {
            case 'w' ->{
                dy[0] = -1;
                wPressed = true;
            }
            case 's' -> {
                dy[0] = 1;
                sPressed = true;
            }
            case 'a' -> {
                dx[0] = -1;
                aPressed = true;
                facingRight = false;
            }
            case 'd' ->{
                dx[0] = 1;
                dPressed = true;
                facingRight = true;
            }
        }

        state = STATE_RUN;

        if ((playerX == 0 && dx[0] == -1) || (playerX == layer1[0].length - 1 && dx[0] == 1)) dx[0] = 0;
        if ((playerY == 0 && playerY == layer1.length - 1 && dy[0] == 1)) dy[0] = 0;

        int newX = playerX + dx[0];
        int newY = playerY + dy[0];

        int mapWidth = layer1[0].length * TILE_SIZE;
        int mapHeight = layer1.length * TILE_SIZE;

        int futureDrawX = newX * TILE_SIZE;
        int futureDrawY = newY * TILE_SIZE;

        /// Coliziunea cu inamicii
        Rectangle futureHitbox = new Rectangle(
                newX * TILE_SIZE + HITBOX_OFFSET_X,
                newY * TILE_SIZE + HITBOX_OFFSET_Y,
                HITBOX_WIDTH,
                HITBOX_HEIGHT
        );

        boolean collisionWithEnemy = false;
        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && futureHitbox.intersects(enemy.getHitbox())) {
                collisionWithEnemy = true;
                break;
            }

        }

        if (collisionWithEnemy) {
            state = STATE_IDLE;
            return; // Nyx nu se mai poate deplasa peste inamic
        }

        /// Atacul lui Nyx
        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && attackHitbox.intersects(enemy.getHitbox())) {
                enemy.die(); // => începe animatia de death
            }
        }

        boolean inBounds = futureDrawX >= 0 && futureDrawX + TILE_SIZE <= mapWidth &&
                futureDrawY >= 0 && futureDrawY + TILE_SIZE <= mapHeight;

        //boolean possible = false;
        boolean baseWalkable = false;
        if (newY >= 0 && newY < layer1.length && newX >= 0 && newX < layer1[0].length) {
            int tile1 = layer1[newY][newX];

            baseWalkable = tile1 == 1567 || tile1 == 212 || tile1 == 213 || tile1 == 286 || tile1 == 287;
        }

        if (inBounds && baseWalkable) {
            isAnimating = true;
            int steps = 4;
            int stepSize = TILE_SIZE / steps;
            Timer timer = new Timer(10, null);
            final int[] count = {0};
            int totalOffsetX = dx[0] * TILE_SIZE;
            int totalOffsetY = dy[0] * TILE_SIZE;

            timer.addActionListener(evt -> {
//                offsetX -= dx[0] * stepSize;
//                offsetY -= dy[0] * stepSize;
                offsetX = totalOffsetX - (totalOffsetX * count[0] / steps);
                offsetY = totalOffsetY - (totalOffsetY * count[0] / steps);

                repaint();
                count[0]++;

                if (count[0] >= steps) {
                    timer.stop();
                    playerX = newX;
                    playerY = newY;
                    showReturnMessage = (playerX == RETURN_TRIGGER_X && playerY == RETURN_TRIGGER_Y);
                    offsetX = 0;
                    offsetY = 0;
                    isAnimating = false;
                    updateCamera();
                    hitbox.x = playerX * TILE_SIZE + HITBOX_OFFSET_X;
                    hitbox.y = playerY * TILE_SIZE + HITBOX_OFFSET_Y;

                    showLevel3Message = (playerX == LEVEL3_TRIGGER_X && playerY == LEVEL3_TRIGGER_Y);

                    repaint();
                }
            });
            offsetX = dx[0] * TILE_SIZE;
            offsetY = dy[0] * TILE_SIZE;
            timer.start();
        } else {
            state = STATE_IDLE;
        }
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        switch (e.getKeyChar()) {
            case 's' -> sPressed = false;
            case 'a' -> aPressed = false;
            case 'd' -> dPressed = false;
            case 'w' -> wPressed = false;
        }
        if(!wPressed && !sPressed && !aPressed && !dPressed)
            state = STATE_IDLE;
    }
}
