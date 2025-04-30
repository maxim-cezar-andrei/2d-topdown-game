import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

class GameMap extends JPanel implements KeyListener {
    private final int TILE_SIZE = 32;
    private final int TILES_PER_ROW = 8;
    private Image tileset;

    private final GameFrame parentFrame;
    private int playerX = 6, playerY = 2;
    private int offsetX = 0, offsetY = 0;
    private Image nyxSprite;
    private boolean isAnimating = false;

    private PauseMenuPanel pauseMenu;
    private boolean menuVisible = false;

    private int[][] layer1;
    private int[][] layer2;

    private int cameraX = 0;
    private int cameraY = 0;

    public GameMap(GameFrame parentFrame) {
        this.parentFrame = parentFrame;

        tileset = new ImageIcon("assets/tiles/void-tiles.png").getImage();
        layer1 = loadCSV("assets/maps/harta_principala._Tile Layer 1.csv");
        layer2 = loadCSV("assets/maps/harta_principala._Tile Layer 2.csv");

        try {
            nyxSprite = new ImageIcon("assets/sprites/nyx_sprite.png").getImage();
        } catch (Exception e) {
            System.out.println("Nyx sprite not found.");
        }

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
        pauseMenu.setBounds(0, 0, getWidth(), getHeight());
        pauseMenu.setVisible(false);
        add(pauseMenu);
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

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(-cameraX, -cameraY);

        drawLayer(g2d, layer1);
        drawLayer(g2d, layer2);

        if (nyxSprite != null) {
            int mapWidth = layer1[0].length * TILE_SIZE;
            int mapHeight = layer1.length * TILE_SIZE;

            int drawX = playerX * TILE_SIZE + offsetX;
            int drawY = playerY * TILE_SIZE + offsetY;

            if (drawX >= 0 && drawX + TILE_SIZE <= mapWidth && drawY >= 0 && drawY + TILE_SIZE <= mapHeight) {
                g2d.drawImage(nyxSprite, drawX, drawY, TILE_SIZE, TILE_SIZE, this);
            }
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

        int viewportWidth = getWidth();
        int viewportHeight = getHeight();

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
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            menuVisible = !menuVisible;
            pauseMenu.setVisible(menuVisible);

            if (menuVisible) {
                pauseMenu.requestFocusInWindow();
            } else {
                requestFocusInWindow();
            }

            repaint();
            return;
        }

        if (isAnimating || menuVisible) return;

        final int[] dx = {0}, dy = {0};

        switch (e.getKeyChar()) {
            case 'w' -> dy[0] = -1;
            case 's' -> dy[0] = 1;
            case 'a' -> dx[0] = -1;
            case 'd' -> dx[0] = 1;
        }

        if ((playerX == 0 && dx[0] == -1) || (playerX == layer1[0].length - 1 && dx[0] == 1)) {
            dx[0] = 0;
        }
        if ((playerY == 0 && dy[0] == -1) || (playerY == layer1.length - 1 && dy[0] == 1)) {
            dy[0] = 0;
        }

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
                    repaint();
                }
            });
            offsetX = dx[0] * TILE_SIZE;
            offsetY = dy[0] * TILE_SIZE;
            timer.start();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}
