package map;

import entities.Coins;
import entities.Enemy;
import entities.HealthItem;
import entities.Nyx;
import main.DataBaseManager;
import main.GameState;
import main.MainMenu;
import ui.HealthBar;
import ui.PauseMenuPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


public class GameMap1 extends JPanel implements KeyListener {
    private final int TILE_SIZE = 32;
    private final int TILES_PER_ROW = 74;
    private Image tileset;

    private final JFrame parentFrame;

    private PauseMenuPanel pauseMenu;
    private boolean menuVisible = false;

    private int[][] layer1;
    private int cameraX = 0;
    private int cameraY = 0;

    private static final int NEXT_LEVEL_X = 2;
    private static final int NEXT_LEVEL_Y = 2;
    private boolean showNextLevelMessage = false;

    private final int RETURN_TRIGGER_X = 6;
    private final int RETURN_TRIGGER_Y = 2;
    private boolean showReturnMessage = false;

    private Nyx nyx;
    private List<Enemy> enemies = new ArrayList<>();
    private int mapId = 1;

    private List<Coins> coins = new ArrayList<>();
    private BufferedImage chipSprite;
    private int score = 0;
    private int totalScore = 0;

    private List<HealthItem> healthItems = new ArrayList<>();
    private HealthBar healthBar = new HealthBar();


    public GameMap1(JFrame parentFrame) {
        this.parentFrame = parentFrame;

        tileset = new ImageIcon("assets/tiles/tileset x2.png").getImage();
        layer1 = loadCSV("assets/maps/nivel1.csv");

        enemies.add(new Enemy(5, 5));
        enemies.add(new Enemy(10, 8));

        nyx = new Nyx(1, 1, enemies);
        nyx.setWalkableTiles(List.of(1567));
        nyx.setRepaintCallback(this::repaint);

        try {
            chipSprite = javax.imageio.ImageIO.read(new File("assets/sprites/Collectibles/crypto_chip_sprite_32x32.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Poziții exemplu (adaptează coordonatele după hartă)
        coins.add(new Coins(10, 5, chipSprite));
        coins.add(new Coins(15, 8, chipSprite));
        coins.add(new Coins(20, 12, chipSprite));

        healthItems.add(new HealthItem(10, 4));
        healthItems.add(new HealthItem(15, 9));
        healthItems.add(new HealthItem(12, 9));
        healthItems.add(new HealthItem(13, 5));
        healthItems.add(new HealthItem(17, 8));

        DataBaseManager db2 = new DataBaseManager();
        db2.resetTotalScore();
        totalScore = db2.getTotalScore();
        db2.close();

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
                    db.saveGame(nyx, enemies, mapId, score, nyx.getHealth());
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
        pauseMenu.setFocusable(false);
        add(pauseMenu);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                pauseMenu.setBounds(0, 0, getWidth(), getHeight());
            }
        });
        startAnimationTimer();
    }

    public GameMap1(JFrame parentFrame, GameState state) {
        this.parentFrame = parentFrame;
        this.nyx = state.getNyx();
        this.enemies = state.getEnemies();
        this.mapId = 1;

        DataBaseManager db2 = new DataBaseManager();
        db2.resetTotalScore();
        totalScore = db2.getTotalScore();
        db2.close();

        this.tileset = new ImageIcon("assets/tiles/tileset x2.png").getImage();
        this.layer1 = loadCSV("assets/maps/nivel1.csv");

        nyx.setWalkableTiles(List.of(1567));
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

        initPauseMenu();
        startAnimationTimer();
    }

    private void initPauseMenu() {
        pauseMenu = new PauseMenuPanel(
                // Resume
                () -> {
                    pauseMenu.setVisible(false);
                    requestFocusInWindow();
                },
                // main.Main Menu
                () -> {
                    parentFrame.dispose();
                    new MainMenu();
                },
                // Save
                () -> {
                    DataBaseManager db = new DataBaseManager();
                    db.saveGame(nyx, enemies, mapId, score, nyx.getHealth());
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
            update();
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

    public void update() {
        nyx.update(layer1, layer1, enemies);

        Iterator<HealthItem> iterator = healthItems.iterator();
        while (iterator.hasNext()) {
            HealthItem item = iterator.next();
            if (nyx.getHitbox().intersects(item.getHitbox())) {
                nyx.heal();            // Vindecă Nyx
                healthBar.setHealth(nyx.getHealth()); // Actualizează HealthBar (dacă ai getter)
                iterator.remove();      // Elimină orb-ul
            }
        }

        for (Coins c : coins) {
            if (!c.isCollected() && c.checkCollision(nyx.getHitbox())) {
                score += c.getPoints();
            }
        }

        for (HealthItem item : healthItems) {
            if (!item.isCollected() && nyx.getHitbox().intersects(item.getHitbox())) {
                item.collect();
                nyx.heal();
            }
        }

        int nyxX = nyx.getX();
        int nyxY = nyx.getY();
        showNextLevelMessage = (nyxX == 2 && nyxY == 2);

        for (Enemy enemy : enemies) {
            enemy.updateAnimation(); // păstrezi animația
        }
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

        for (Coins c : coins) {
            c.draw(g2d);
            if (!c.isCollected()) {
                g2d.setColor(Color.YELLOW);
                Rectangle chipHitbox = new Rectangle(c.getX() * 32, c.getY() * 32, 32, 32);
                g2d.drawRect(chipHitbox.x, chipHitbox.y, chipHitbox.width, chipHitbox.height);
            }
        }

        nyx.draw(g2d);

        for (Enemy enemy : enemies) {
            enemy.draw(g2d, TILE_SIZE);
            if (nyx.getHitbox().intersects(enemy.getHitbox())) {
                System.out.println("Coliziune cu inamicul!");
            }
        }

        for (HealthItem item : healthItems) {
            item.draw(g2d);
        }

        if (showReturnMessage) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("Press E to return to main map", cameraX + 50, cameraY + 50);
        }

        if (showNextLevelMessage) {
            g2d.setColor(Color.CYAN);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("Press E if you want to advance to next level", cameraX + 50, cameraY + 70);
        }

        // Desenare scor
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 20, 30);
        g.drawString("Total: " + totalScore, 20, 50);

        healthBar.draw(g2d, nyx, cameraX+120,cameraY+20);


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

        if (e.getKeyChar() == 'e' && showNextLevelMessage) {
            DataBaseManager db = new DataBaseManager();
            db.saveGame(nyx, enemies, mapId, score, nyx.getHealth());
            db.close();

            GameMap2 nextMap = new GameMap2(parentFrame);
            nextMap.setFocusable(true);
            nextMap.requestFocusInWindow();
            parentFrame.setContentPane(nextMap);
            parentFrame.revalidate();
            parentFrame.pack();
            parentFrame.repaint();
            SwingUtilities.invokeLater(nextMap::requestFocusInWindow);
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
