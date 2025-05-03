import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class GameMap extends JPanel implements KeyListener {
    private final int TILE_SIZE = 32;
    private final int TILES_PER_ROW = 8;
    private Image tileset;

    private final JFrame parentFrame;
    private int playerX = 6, playerY = 2;
    private int offsetX = 0, offsetY = 0;
    private boolean isAnimating = false;

    private PauseMenuPanel pauseMenu;
    private boolean menuVisible = false;

    private int[][] layer1;
    private int[][] layer2;

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
    private final int HITBOX_HEIGHT = 24;
    private final int HITBOX_OFFSET_X = 4;
    private final int HITBOX_OFFSET_Y = 8;

    private boolean wPressed, aPressed, sPressed, dPressed;

    public GameMap(JFrame parentFrame) {
        this.parentFrame = parentFrame;

        tileset = new ImageIcon("assets/tiles/void-tiles.png").getImage();
        layer1 = loadCSV("assets/maps/harta_principala._Tile Layer 1.csv");
        layer2 = loadCSV("assets/maps/harta_principala._Tile Layer 2.csv");

        loadPlayerSprites();
        startAnimation();

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

        pauseMenu = new PauseMenuPanel(
                () -> { parentFrame.dispose(); new MainMenu(); },
                () -> { JOptionPane.showMessageDialog(this, "Load not implemented yet."); },
                () -> { JOptionPane.showMessageDialog(this, "Options coming soon!"); },
                () -> { System.exit(0); }
        );
        pauseMenu.setBounds(0, 0, 800, 600);
        pauseMenu.setVisible(false);
        add(pauseMenu);
    }

    private void loadPlayerSprites() {
        try {
            BufferedImage idleSheet = javax.imageio.ImageIO.read(new File("assets/sprites/Idle.png"));
            BufferedImage runSheet = javax.imageio.ImageIO.read(new File("assets/sprites/Run.png"));

            idleFrames = new Image[IDLE_FRAMES];
            runFrames = new Image[RUN_FRAMES];

            for (int i = 0; i < IDLE_FRAMES; i++) {
                BufferedImage frame = idleSheet.getSubimage(i * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT);
                idleFrames[i] = frame.getScaledInstance(50, 80, Image.SCALE_SMOOTH);
            }

            for (int i = 0; i < RUN_FRAMES; i++) {
                BufferedImage frame = runSheet.getSubimage(i * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT);
                runFrames[i] = frame.getScaledInstance(50, 80, Image.SCALE_SMOOTH);
            }

        } catch (Exception e) {
            System.out.println("Eroare la încărcarea sprite-urilor ninja:");
            e.printStackTrace();
        }
    }

    private void startAnimation() {
        animationTimer = new Timer(120, e -> {
            currentFrame++;
            if (state == STATE_IDLE) currentFrame %= IDLE_FRAMES;
            else currentFrame %= RUN_FRAMES;
            repaint();
        });
        animationTimer.start();
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
        drawLayer(g2d, layer2);

        int spriteWidth = 50;
        int spriteHeight = 80;

        int drawX = playerX * TILE_SIZE + offsetX + (TILE_SIZE - spriteWidth) / 2;
        int drawY = playerY * TILE_SIZE + offsetY + (TILE_SIZE - spriteHeight) / 2;

        if (state == STATE_IDLE && idleFrames != null && idleFrames[currentFrame] != null)
            g2d.drawImage(idleFrames[currentFrame], drawX, drawY, this);
        else if (state == STATE_RUN && runFrames != null && runFrames[currentFrame] != null)
            g2d.drawImage(runFrames[currentFrame], drawX, drawY, this);
        else {
            g2d.setColor(Color.RED);
            g2d.fillRect(drawX, drawY, TILE_SIZE, TILE_SIZE);
        }

        // Draw hitbox for debugging
        g2d.setColor(Color.GREEN);
        g2d.draw(hitbox);

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

        if (isAnimating || menuVisible) return;

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
            }
            case 'd' ->{
                dx[0] = 1;
                dPressed = true;
            }
        }

        /*
        if (dx[0] != 0 || dy[0] != 0)
            state = STATE_RUN;
        else
            state = STATE_IDLE;
        */
        state = STATE_RUN;

        if ((playerX == 0 && dx[0] == -1) || (playerX == layer1[0].length - 1 && dx[0] == 1)) dx[0] = 0;
        if ((playerY == 0 && playerY == layer1.length - 1 && dy[0] == 1)) dy[0] = 0;

        int newX = playerX + dx[0];
        int newY = playerY + dy[0];

        int mapWidth = layer1[0].length * TILE_SIZE;
        int mapHeight = layer1.length * TILE_SIZE;

        int futureDrawX = newX * TILE_SIZE;
        int futureDrawY = newY * TILE_SIZE;

        boolean inBounds = futureDrawX >= 0 && futureDrawX + TILE_SIZE <= mapWidth &&
                futureDrawY >= 0 && futureDrawY + TILE_SIZE <= mapHeight;

        boolean possible = false;
        if (newY >= 0 && newY < layer1.length && newX >= 0 && newX < layer1[0].length) {
            int tile1 = layer1[newY][newX];
            int tile2 = layer2[newY][newX];

            boolean baseWalkable = tile1 == 2 || tile1 == 17 || tile2 == 39 || tile2 == 47;
            boolean specialWalkable = tile2 == 111 || tile2 == 119;

            possible = baseWalkable || specialWalkable;
        }

        if (inBounds && possible) {
            isAnimating = true;
            int steps = 2;
            int stepSize = TILE_SIZE / steps;
            Timer timer = new Timer(10, null);
            final int[] count = {0};
            timer.addActionListener(evt -> {
                offsetX -= dx[0] * stepSize;
                offsetY -= dy[0] * stepSize;
                count[0]++;
                repaint();
                if (count[0] >= steps) {
                    timer.stop();
                    playerX = newX;
                    playerY = newY;
                    offsetX = 0;
                    offsetY = 0;
                    isAnimating = false;
                    updateCamera();
                    hitbox.x = playerX * TILE_SIZE + HITBOX_OFFSET_X;
                    hitbox.y = playerY * TILE_SIZE + HITBOX_OFFSET_Y;
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
