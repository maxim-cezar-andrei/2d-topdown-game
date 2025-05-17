import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class GameMap extends JPanel implements KeyListener {
    private final int TILE_SIZE = 32;
    private final int TILES_PER_ROW = 8;
    private Image tileset;

    private final JFrame parentFrame;

    private PauseMenuPanel pauseMenu;
    private boolean menuVisible = false;

    private int[][] layer1;
    private int[][] layer2;

    private int cameraX = 0;
    private int cameraY = 0;

    private final int LEVEL1_TRIGGER_X =22;
    private final int LEVEL1_TRIGGER_Y = 5;
    private boolean showLevel1Message = false;

    private final int LEVEL2_TRIGGER_X = 1;
    private final int LEVEL2_TRIGGER_Y = 9;
    private boolean showLevel2Message = false;

    private final int LEVEL3_TRIGGER_X = 9;
    private final int LEVEL3_TRIGGER_Y = 16;
    private boolean showLevel3Message = false;

    private Nyx nyx;
    private List<Enemy> enemies = new ArrayList<>();
    private int mapId = 0;

    public GameMap(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        tileset = new ImageIcon("assets/tiles/void-tiles.png").getImage();
        layer1 = loadCSV("assets/maps/harta_principala._Tile Layer 1.csv");
        layer2 = loadCSV("assets/maps/harta_principala._Tile Layer 2.csv");

        nyx = new Nyx(6, 2, enemies);
        nyx.setWalkableTiles(List.of(2, 17));

        nyx.setRepaintCallback(this::repaint);


        enemies.add(new Enemy(8, 8));
        enemies.add(new Enemy(6, 9));
        enemies.add(new Enemy(5, 10));

        setLayout(null);
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                nyx.handleMousePressed(e);
                nyx.checkAttack(enemies);
            }
        });

        pauseMenu = new PauseMenuPanel(
                () -> {
                    pauseMenu.setVisible(false);
                    menuVisible = false;
                    requestFocusInWindow();
                },
                () -> { parentFrame.dispose(); new MainMenu(); },
                () -> {
                    DataBaseManager db = new DataBaseManager();
                    db.saveGame(nyx, enemies, 0);
                    db.close();
                    JOptionPane.showMessageDialog(this, "Game saved!");
                    pauseMenu.requestFocusInWindow();
                },
                () -> {
                    DataBaseManager db = new DataBaseManager();
                    GameState state = db.loadLastGame();
                    db.close();

                    if (state != null) {
                        this.nyx = state.getNyx();
                        this.enemies = state.getEnemies();
                        nyx.setWalkableTiles(List.of(2, 17));
                        nyx.setRepaintCallback(this::repaint);
                        JOptionPane.showMessageDialog(this, "Game loaded!");
                        pauseMenu.requestFocusInWindow();
                    } else {
                        JOptionPane.showMessageDialog(this, "No saved game found.");
                    }
                },
                () -> { JOptionPane.showMessageDialog(this, "Options coming soon!"); },
                () -> { System.exit(0); }
        );



        pauseMenu.setBounds(0, 0, getWidth(), getHeight());
        pauseMenu.setVisible(false);
        pauseMenu.setFocusable(false);
        add(pauseMenu);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                pauseMenu.setBounds(0, 0, getWidth(), getHeight());
            }
        });

        new Timer(120, e -> {
            nyx.update(layer1, layer2, enemies);

            int nyxX = nyx.getX();
            int nyxY = nyx.getY();

            showLevel1Message = (nyxX == LEVEL1_TRIGGER_X && nyxY == LEVEL1_TRIGGER_Y);
            showLevel2Message = (nyxX == LEVEL2_TRIGGER_X && nyxY == LEVEL2_TRIGGER_Y);
            showLevel3Message = (nyxX == LEVEL3_TRIGGER_X && nyxY == LEVEL3_TRIGGER_Y);

            for (Enemy enemy : enemies) {
                enemy.updateAnimation();
            }
            repaint();
        }).start();
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

        cameraX = 6 * TILE_SIZE - viewportWidth / 2 + TILE_SIZE / 2;
        cameraY = 2 * TILE_SIZE - viewportHeight / 2 + TILE_SIZE / 2;

        cameraX = Math.max(0, Math.min(cameraX, mapWidth - viewportWidth));
        cameraY = Math.max(0, Math.min(cameraY, mapHeight - viewportHeight));
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

        nyx.draw(g2d);

        for (Enemy enemy : enemies) {
            enemy.updateAnimation();
            enemy.updateHitbox(TILE_SIZE);
            enemy.draw(g2d, TILE_SIZE);

            if (nyx.getHitbox().intersects(enemy.getHitbox())) {
                System.out.println("Coliziune cu inamicul!");
            }
        }

        if (showLevel1Message) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("Press 1 if you want to enter level 1", cameraX + 50, cameraY + 50);
        }
        if (showLevel2Message) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("Press 2 if you want to enter level 2", cameraX + 50, cameraY + 50);
        }
        if (showLevel3Message) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("Press 3 if you want to enter level 3", cameraX + 50, cameraY + 50);
        }

        g2d.dispose();
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

        if (e.getKeyChar() == '1' && showLevel1Message) {
            GameMap1 gameMap1 = new GameMap1(parentFrame);
            gameMap1.setFocusable(true);
            gameMap1.requestFocusInWindow();
            parentFrame.setContentPane(gameMap1);
            parentFrame.revalidate();
            parentFrame.pack();
            parentFrame.repaint();
            SwingUtilities.invokeLater(gameMap1::requestFocusInWindow);
            return;
        }

        if (e.getKeyChar() == '2' && showLevel2Message) {
            GameMap2 gameMap2 = new GameMap2(parentFrame);
            gameMap2.setFocusable(true);
            gameMap2.requestFocusInWindow();
            parentFrame.setContentPane(gameMap2);
            parentFrame.revalidate();
            parentFrame.pack();
            parentFrame.repaint();
            SwingUtilities.invokeLater(gameMap2::requestFocusInWindow);
            return;
        }

        if (e.getKeyChar() == '3' && showLevel3Message) {
            GameMap3 gameMap3 = new GameMap3(parentFrame);
            gameMap3.setFocusable(true);
            gameMap3.requestFocusInWindow();
            parentFrame.setContentPane(gameMap3);
            parentFrame.revalidate();
            parentFrame.pack();
            parentFrame.repaint();
            SwingUtilities.invokeLater(gameMap3::requestFocusInWindow);
            return;
        }


        nyx.handleKeyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        nyx.handleKeyReleased(e);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(layer1[0].length * TILE_SIZE, layer1.length * TILE_SIZE);
    }
}
