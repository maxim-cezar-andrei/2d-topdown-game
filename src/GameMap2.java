import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class GameMap2 extends JPanel implements KeyListener {
    private final int TILE_SIZE = 32;
    private final int TILES_PER_ROW = 74;
    private Image tileset;

    private final JFrame parentFrame;

    private PauseMenuPanel pauseMenu;
    private boolean menuVisible = false;

    private int[][] layer1;
    private int cameraX = 0;
    private int cameraY = 0;

    private final int RETURN_TRIGGER_X = 6;
    private final int RETURN_TRIGGER_Y = 2;
    private boolean showReturnMessage = false;

    private Nyx nyx;
    private List<Enemy> enemies = new ArrayList<>();
    private int mapId = 2;

    public GameMap2(JFrame parentFrame) {
        this.parentFrame = parentFrame;

        tileset = new ImageIcon("assets/tiles/tileset x2.png").getImage();
        layer1 = loadCSV("assets/maps/nivel2.csv");

        enemies.add(new Enemy(5, 5));
        enemies.add(new Enemy(10, 8));

        nyx = new Nyx(1, 1, enemies);
        nyx.setRepaintCallback(this::repaint);
        nyx.setWalkableTiles(List.of(225));

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
                    requestFocusInWindow();  // revenim la joc
                },
                () -> { parentFrame.dispose(); new MainMenu(); },
                () -> {
                    DataBaseManager db = new DataBaseManager();
                    db.saveGame(nyx, enemies, mapId);
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
                        nyx.setWalkableTiles(List.of(225));
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
            nyx.update(layer1, layer1, enemies);

            int nyxX = nyx.getX();
            int nyxY = nyx.getY();
            showReturnMessage = (nyxX == RETURN_TRIGGER_X && nyxY == RETURN_TRIGGER_Y);

            for (Enemy enemy : enemies) enemy.updateAnimation();
            repaint();
        }).start();
    }

    public GameMap2(JFrame parentFrame, GameState state) {
        this.parentFrame = parentFrame;
        this.nyx = state.getNyx();
        this.enemies = state.getEnemies();
        this.mapId = 2;

        this.tileset = new ImageIcon("assets/tiles/tileset x2.png").getImage();
        this.layer1 = loadCSV("assets/maps/nivel2.csv");

        nyx.setWalkableTiles(List.of(225));
        nyx.setRepaintCallback(this::repaint);

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

        initPauseMenu(); // dacă ai extras codul într-o metodă
        startAnimationTimer(); // dacă ai extras timerul într-o metodă
    }

    private void initPauseMenu() {
        pauseMenu = new PauseMenuPanel(
                // Resume
                () -> {
                    pauseMenu.setVisible(false);
                    requestFocusInWindow();
                },
                // Main Menu
                () -> {
                    parentFrame.dispose();
                    new MainMenu();
                },
                // Save
                () -> {
                    DataBaseManager db = new DataBaseManager();
                    db.saveGame(nyx, enemies, mapId);
                    db.close();
                    JOptionPane.showMessageDialog(this, "Game saved!");
                    pauseMenu.requestFocusInWindow();
                },
                // Load
                () -> {
                    DataBaseManager db = new DataBaseManager();
                    GameState state = db.loadLastGame();
                    db.close();
                    if (state != null) {
                        this.nyx = state.getNyx();
                        this.enemies = state.getEnemies();
                        nyx.setWalkableTiles(List.of(1567));
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
        add(pauseMenu);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                pauseMenu.setBounds(0, 0, getWidth(), getHeight());
            }
        });
    }

    private void startAnimationTimer() {
        new Timer(120, e -> {
            nyx.update(layer1, layer1, enemies);

            int nyxX = nyx.getX();
            int nyxY = nyx.getY();

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

        int nyxX = nyx.getX();
        int nyxY = nyx.getY();

        cameraX = nyxX * TILE_SIZE - viewportWidth / 2 + TILE_SIZE / 2;
        cameraY = nyxY * TILE_SIZE - viewportHeight / 2 + TILE_SIZE / 2;

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
        nyx.draw(g2d);

        for (Enemy enemy : enemies) {
            enemy.draw(g2d, TILE_SIZE);
            if (nyx.getHitbox().intersects(enemy.getHitbox())) {
                System.out.println("Coliziune cu inamicul!");
            }
        }

        if (showReturnMessage) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("Press E to return to main map", cameraX + 50, cameraY + 50);
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
            if (menuVisible) pauseMenu.requestFocusInWindow();
            else requestFocusInWindow();
            repaint();
            return;
        }

        if (e.getKeyChar() == 'e' && showReturnMessage) {
            GameMap mainMap = new GameMap(parentFrame);
            mainMap.setFocusable(true);
            mainMap.requestFocusInWindow();
            parentFrame.setContentPane(mainMap);
            parentFrame.revalidate();
            parentFrame.pack();
            parentFrame.repaint();
            SwingUtilities.invokeLater(mainMap::requestFocusInWindow);
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
