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

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class GameMap3 extends JPanel implements KeyListener {
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

    private final int LEVEL3_TRIGGER_X = 16;
    private final int LEVEL3_TRIGGER_Y = 9;
    private boolean showLevel3Message = false;

    private Nyx nyx;
    private List<Enemy> enemies = new ArrayList<>();
    private int mapId = 3;

    private Coins finalChip;
    private BufferedImage finalChipSprite;
    private List<Coins> coins = new ArrayList<>();
    private BufferedImage chipSprite;
    private int score = 0;
    private int totalScore = 0;

    private List<HealthItem> healthItems = new ArrayList<>();
    private HealthBar healthBar = new HealthBar();


    public GameMap3(JFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.mapId = 3;

        tileset = new ImageIcon("assets/tiles/tileset x2.png").getImage();
        layer1 = loadCSV("assets/maps/nivel1.csv");

        enemies.add(new Enemy(5, 5));
        enemies.add(new Enemy(10, 8));

        nyx = new Nyx(1, 1, enemies);
        nyx.setRepaintCallback(this::repaint);
        nyx.setWalkableTiles(List.of(1567, 212, 213, 286, 287, 360, 361, 434, 435));

        try {
            chipSprite = javax.imageio.ImageIO.read(new File("assets/sprites/Collectibles/crypto_chip_sprite_32x32.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            finalChipSprite = ImageIO.read(new File("assets/sprites/Collectibles/final_chip_32x32.png"));
            finalChip = new Coins(48, 17, finalChipSprite);
            coins.add(finalChip);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Inamici
        generateRandomEnemies(20);

        //HealthItem
        generateRandomHealthItems(40);

        // Coins
        generateRandomCoins(50);

        DataBaseManager db2 = new DataBaseManager();
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
                        nyx.setWalkableTiles(List.of(1567, 212, 213, 286, 287, 360, 361, 434, 435));
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

    public GameMap3(JFrame parentFrame, GameState state) {
        this.parentFrame = parentFrame;
        this.nyx = state.getNyx();
        this.enemies = state.getEnemies();
        this.mapId = 3;

        DataBaseManager db2 = new DataBaseManager();
        totalScore = db2.getTotalScore();
        db2.close();

        this.tileset = new ImageIcon("assets/tiles/tileset x2.png").getImage();
        this.layer1 = loadCSV("assets/maps/nivel1.csv");

        nyx.setWalkableTiles(List.of(1567, 212, 213, 286, 287, 360, 361, 434, 435));
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
                        nyx.setWalkableTiles(List.of(1567, 212, 213, 286, 287, 360, 361, 434, 435));
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

            Rectangle nyxHitbox = nyx.getHitbox();
            for (Coins c : coins) {
                if (!c.isCollected() && c.checkCollision(nyxHitbox)) {
                    score += c.getPoints();
                    if (c == finalChip) {
                        endGame();
                    }
                }
            }

            int nyxX = nyx.getX();
            int nyxY = nyx.getY();

            for (Enemy enemy : enemies) {
                enemy.updateAnimation();
            }
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

    private void endGame() {
        DataBaseManager db = new DataBaseManager();
        db.saveGame(nyx, enemies, mapId, score, nyx.getHealth()); // salvează și ultimul scor
        int total = db.getTotalScore();
        db.close();

        JOptionPane.showMessageDialog(this,
                "🎉 Ai terminat jocul!\nScor total: " + total,
                "Felicitări!",
                JOptionPane.INFORMATION_MESSAGE);

        parentFrame.dispose();
        new MainMenu();// sau: new MainMenu(); dacă ai meniu
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

        if (!nyx.isNyxDead()) {
            for (Enemy enemy : enemies) {
                enemy.Proximity(nyx.getX(), nyx.getY());
                if (!enemy.isDead() && enemy.isNear(nyx.getX(), nyx.getY())) {
                    if (enemy.canAttack()) {
                        nyx.takeDamage();           // se ocupă singur de cooldown
                        enemy.startAttack();        // animatie vizuală
                        enemy.resetAttackCooldown(); // cooldown pentru inamic (opțional)
                    }
                }
                enemy.updateDirection(nyx.getX());
                enemy.updateAnimation();
            }
        }


        repaint();
    }

    private void generateRandomEnemies(int count) {
        Random random = new Random();
        int tileSize = TILE_SIZE; // Folosește constanta existentă
        int mapWidth = layer1[0].length;
        int mapHeight = layer1.length;

        System.out.println("Generating enemies on map size: " + mapWidth + "x" + mapHeight);

        int generated = 0;
        int attempts = 0;
        int maxAttempts = count * 10;

        while (generated < count && attempts < maxAttempts) {
            int tileX = random.nextInt(mapWidth);
            int tileY = random.nextInt(mapHeight);

            if (layer1[tileY][tileX] == 1567) {

                enemies.add(new Enemy(tileX, tileY));
                generated++;
            }
            attempts++;
        }

        System.out.println("Generated " + generated + " enemies");
        if (generated < count) {
            System.out.println("Warning: Could only place " + generated + " out of " + count + " enemies");
        }
    }
    private void generateRandomCoins(int count) {
        Random random = new Random();
        int tileSize = TILE_SIZE;
        int mapWidth = layer1[0].length;
        int mapHeight = layer1.length;

        int generated = 0;
        int maxAttempts = count * 10;

        while (generated < count && maxAttempts > 0) {
            int tileX = random.nextInt(mapWidth);
            int tileY = random.nextInt(mapHeight);

            if (layer1[tileY][tileX] == 1567 && !isCoinPositionOccupied(tileX, tileY)) {
                coins.add(new Coins(tileX, tileY, chipSprite));
                generated++;
                System.out.println("Coin generat la: (" + tileX + ", " + tileY + ")");
            }

            maxAttempts--;
        }

        System.out.println("Au fost generate " + generated + " monede.");
    }
    private boolean isCoinPositionOccupied(int tileX, int tileY) {
        for (Coins c : coins) {
            if (c.getX() == tileX && c.getY() == tileY) {
                return true;
            }
        }
        return false;
    }
    private void generateRandomHealthItems(int count) {
        Random random = new Random();
        int tileSize = TILE_SIZE;
        int mapWidth = layer1[0].length;
        int mapHeight = layer1.length;

        int generated = 0;
        int maxAttempts = count * 10;

        while (generated < count && maxAttempts > 0) {
            int tileX = random.nextInt(mapWidth);
            int tileY = random.nextInt(mapHeight);

            if (layer1[tileY][tileX] == 1567 && isPositionSafe(tileX, tileY, 3)) {
                healthItems.add(new HealthItem(tileX, tileY));
                generated++;
                System.out.println("HealthItem generat la: (" + tileX + ", " + tileY + ")");
            }

            maxAttempts--;
        }

        System.out.println("Au fost generate " + generated + " iteme de viață");
    }

    private boolean isPositionSafe(int tileX, int tileY, int minDistance) {
        // Distanță față de Nyx
        if (Math.abs(tileX - nyx.getX()/TILE_SIZE) < minDistance ||
                Math.abs(tileY - nyx.getY()/TILE_SIZE) < minDistance) {
            return false;
        }

        // Distanță față de inamici
        for (Enemy enemy : enemies) {
            if (Math.abs(tileX - enemy.getX()/TILE_SIZE) < minDistance ||
                    Math.abs(tileY - enemy.getY()/TILE_SIZE) < minDistance) {
                return false;
            }
        }

        for (HealthItem item : healthItems) {
            if (Math.abs(tileX - item.getHitbox().x/TILE_SIZE) < minDistance ||
                    Math.abs(tileY - item.getHitbox().y/TILE_SIZE) < minDistance) {
                return false;
            }
        }

        return !isPositionOccupied(tileX, tileY);
    }
    private boolean isPositionOccupied(int tileX, int tileY) {
        for (HealthItem item : healthItems) {
            if (item.getHitbox().x / TILE_SIZE == tileX &&
                    item.getHitbox().y / TILE_SIZE == tileY) {
                return true;
            }
        }
        return false;
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

        if (showLevel3Message) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("Press 3 if you want to enter level 3", cameraX + 50, cameraY + 50);
        }
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