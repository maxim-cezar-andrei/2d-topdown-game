package entities;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Nyx {
    public static final int STATE_IDLE = 0;
    public static final int STATE_RUN = 1;
    public static final int STATE_ATTACK = 2;

    private int x, y;
    private int offsetX = 0, offsetY = 0;
    private int state = STATE_IDLE;
    private boolean facingRight = true;

    private boolean wPressed, aPressed, sPressed, dPressed;

    private final int TILE_SIZE = 32;
    private final int HITBOX_WIDTH = 28;
    private final int HITBOX_HEIGHT = 28;
    private final int HITBOX_OFFSET_X = 2;
    private final int HITBOX_OFFSET_Y = 2;

    private final int ATTACK_HITBOX_WIDTH = 40;
    private final int ATTACK_HITBOX_HEIGHT = 24;
    private final int ATTACK_HITBOX_OFFSET_Y = 10;
    private final int ATTACK_HITBOX_RIGHT_OFFSET = 24;

    private Image[] idleFrames;
    private Image[] runFrames;
    private Image[] attackFrames;
    private Image[] deathFrames;

    private int currentFrame = 0;
    private int currentDeathFrame = 0;

    private final int FRAME_WIDTH = 200;
    private final int FRAME_HEIGHT = 200;

    private final int IDLE_FRAMES = 4;
    private final int RUN_FRAMES = 8;
    private final int ATTACK_FRAMES = 4;
    private final int DEATH_FRAMES = 7;

    private boolean isDying = false;

    private Timer animationTimer;
    private Rectangle hitbox;
    private Rectangle attackHitbox = new Rectangle();

    private List<Enemy> enemies;

    private List<Integer> walkableTiles = new ArrayList<>();

    private int health = 3;
    private final int maxHealth = 3;
    private long lastHitTime = 0;
    private final int invulnTime = 1000; // ms

    private int dx = 0;
    private int dy = 0;


    public Nyx(int x, int y, List<Enemy> enemies)
    {
        this.x = x;
        this.y = y;
        this.enemies=enemies;
        loadSprites();
        initHitbox();
        startAnimation();
    }

    public void setWalkableTiles(List<Integer> tiles) {
        this.walkableTiles = tiles;
    }

    private void loadSprites() {
        try {
            BufferedImage idleSheet = ImageIO.read(new File("assets/sprites/NyxSprites/Idle.png"));
            BufferedImage runSheet = ImageIO.read(new File("assets/sprites/NyxSprites/Run.png"));
            BufferedImage attackSheet = ImageIO.read(new File("assets/sprites/NyxSprites/Attack2.png"));
            BufferedImage deathSheet = ImageIO.read(new File("assets/sprites/NyxSprites/Death.png"));


            idleFrames = new Image[IDLE_FRAMES];
            runFrames = new Image[RUN_FRAMES];
            attackFrames = new Image[ATTACK_FRAMES];
            deathFrames = new Image[DEATH_FRAMES];

            for (int i = 0; i < IDLE_FRAMES; i++)
                idleFrames[i] = idleSheet.getSubimage(i * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT).getScaledInstance(150, 140, Image.SCALE_SMOOTH);

            for (int i = 0; i < RUN_FRAMES; i++)
                runFrames[i] = runSheet.getSubimage(i * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT).getScaledInstance(150, 140, Image.SCALE_SMOOTH);

            for (int i = 0; i < ATTACK_FRAMES; i++)
                attackFrames[i] = attackSheet.getSubimage(i * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT).getScaledInstance(150, 140, Image.SCALE_SMOOTH);

            for (int i = 0; i < DEATH_FRAMES; i++)
                deathFrames[i] = deathSheet.getSubimage(i * FRAME_WIDTH, 0, FRAME_WIDTH, FRAME_HEIGHT).getScaledInstance(150, 140, Image.SCALE_SMOOTH);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAnimation() {
        animationTimer = new Timer(120, e -> {

            if (isDying) {
                if (currentDeathFrame < DEATH_FRAMES - 1) {
                    currentDeathFrame++;
                }
                repaintCallback.run();
                return;
            }

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
                            ? x * TILE_SIZE + ATTACK_HITBOX_RIGHT_OFFSET
                            : x * TILE_SIZE - ATTACK_HITBOX_WIDTH;

                    attackHitbox.setBounds(
                            attackX,
                            y * TILE_SIZE + ATTACK_HITBOX_OFFSET_Y,
                            ATTACK_HITBOX_WIDTH,
                            ATTACK_HITBOX_HEIGHT
                    );

                    for (Enemy enemy : enemies) {
                        if (!enemy.isDead()
                                && y == enemy.getY()
                                && attackHitbox.intersects(enemy.getHitbox())) {
                            enemy.die();
                            enemy.updateAnimation();
                            System.out.println("Inamic lovit!");
                        }
                    }

                    if (currentFrame >= ATTACK_FRAMES) {
                        currentFrame = 0;
                        state = (wPressed || aPressed || sPressed || dPressed) ? STATE_RUN : STATE_IDLE;
                    }

                }
            }
            repaintCallback.run();

        });
        animationTimer.start();
    }

    private Runnable repaintCallback = () -> {};

    public void setRepaintCallback(Runnable callback) {
        this.repaintCallback = callback;
    }

    private void updateAttackHitbox() {
        int attackX = facingRight ? x * TILE_SIZE + ATTACK_HITBOX_RIGHT_OFFSET : x * TILE_SIZE - ATTACK_HITBOX_WIDTH;
        attackHitbox.setBounds(attackX, y * TILE_SIZE + ATTACK_HITBOX_OFFSET_Y, ATTACK_HITBOX_WIDTH, ATTACK_HITBOX_HEIGHT);
    }

    private boolean isMoving() {
        return wPressed || aPressed || sPressed || dPressed;
    }

    private Image flipImageHorizontally(Image img) {
        BufferedImage original = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = original.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-original.getWidth(), 0);
        return new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR).filter(original, null);
    }

    public void draw(Graphics2D g2d) {
        Image sprite;

        if (isDying) {
            sprite = deathFrames[Math.min(currentDeathFrame, DEATH_FRAMES - 1)];

            if (sprite != null) {
                if (!facingRight) sprite = flipImageHorizontally(sprite);
                int drawX = x * TILE_SIZE + offsetX + (TILE_SIZE - 150) / 2;
                int drawY = y * TILE_SIZE + offsetY + (TILE_SIZE - 150) / 2;
                g2d.drawImage(sprite, drawX, drawY, null);
            }

            return; // moartea oprește orice altceva
        }

        sprite = switch (state) {
            case STATE_IDLE -> idleFrames[currentFrame % IDLE_FRAMES];
            case STATE_RUN -> runFrames[currentFrame % RUN_FRAMES];
            case STATE_ATTACK -> attackFrames[Math.min(currentFrame, ATTACK_FRAMES - 1)];
            default -> null;
        };

        if (sprite != null) {
            if (!facingRight) sprite = flipImageHorizontally(sprite);
            int drawX = x * TILE_SIZE + offsetX + (TILE_SIZE - 150) / 2;
            int drawY = y * TILE_SIZE + offsetY + (TILE_SIZE - 150) / 2;
            g2d.drawImage(sprite, drawX, drawY, null);
        }

        g2d.setColor(Color.GREEN);
        g2d.draw(getHitbox());

        g2d.setColor(Color.BLUE);
        int futurePixelX = (x + dx) * TILE_SIZE + HITBOX_OFFSET_X;
        int futurePixelY = (y + dy) * TILE_SIZE + HITBOX_OFFSET_Y;
        g2d.drawRect(futurePixelX, futurePixelY, HITBOX_WIDTH, HITBOX_HEIGHT);

        if (state == STATE_ATTACK) {
            g2d.setColor(Color.BLUE);
            g2d.draw(attackHitbox);
        }
    }

    public void handleKeyPressed(KeyEvent e) {
        if (isDying) return;
        switch (e.getKeyChar()) {
            case 'w' -> wPressed = true;
            case 'a' -> { aPressed = true; facingRight = false; }
            case 's' -> sPressed = true;
            case 'd' -> { dPressed = true; facingRight = true; }
        }
        if (state != STATE_ATTACK) {
            state = STATE_RUN;
            currentFrame = 0;
        }

    }

    public void handleKeyReleased(KeyEvent e) {
        if (isDying) return;
        switch (e.getKeyChar()) {
            case 'w' -> wPressed = false;
            case 'a' -> aPressed = false;
            case 's' -> sPressed = false;
            case 'd' -> dPressed = false;
        }
        if (!isMoving() && state != STATE_ATTACK)
            state = STATE_IDLE;
    }


    public void handleMousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            currentFrame = 0;
            state = STATE_ATTACK;
        }
    }

    public void checkAttack(List<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && enemy.getY() == y && attackHitbox.intersects(enemy.getHitbox())) {
                enemy.die();
                enemy.updateAnimation();
                System.out.println("Inamic lovit!");
            }
        }
    }

    public void takeDamage() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastHitTime < invulnTime)
            return;
        if (isDying) return;
        health--;
        System.out.println("Nyx a fost lovit! HP ramas: " + health);

        if (health < 0) {
            health = 0;
            isDying = true;
            currentDeathFrame = 0;
            System.out.println("Nyx a murit!");
        }
        lastHitTime = currentTime;
    }

    public void heal() {
        if (!isDying) {
            health++;
            if (health > maxHealth)
                health = maxHealth;
            System.out.println("Nyx s-a vindecat! Viata: " + health);
        }
    }

    public boolean isNyxDead() {
        return isDying && currentDeathFrame >= DEATH_FRAMES - 1;
    }

    private void initHitbox() {
        hitbox = new Rectangle(x * TILE_SIZE + HITBOX_OFFSET_X, y * TILE_SIZE + HITBOX_OFFSET_Y, HITBOX_WIDTH, HITBOX_HEIGHT);
    }

    public void updateHitbox(int TILE_SIZE) {
        hitbox.setLocation(x * TILE_SIZE + HITBOX_OFFSET_X, y * TILE_SIZE + HITBOX_OFFSET_Y);
    }

    public Rectangle getHitbox() {
        return new Rectangle(
                x * TILE_SIZE + HITBOX_OFFSET_X,
                y * TILE_SIZE + HITBOX_OFFSET_Y,
                HITBOX_WIDTH, HITBOX_HEIGHT
        );
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(health, maxHealth));
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void resetHealth() {
        health = maxHealth;
    }

    public void update(int[][] layer1, int[][] layer2, List<Enemy> enemies) {
        dx = 0;
        dy = 0;
        if (isDying) {
            return;
        }
        if (wPressed) dy = -1;
        if (sPressed) dy = 1;
        if (aPressed) dx = -1;
        if (dPressed) dx = 1;

        int newX = x + dx;
        int newY = y + dy;

        Rectangle futureHitbox = new Rectangle(
                newX * TILE_SIZE + HITBOX_OFFSET_X,
                newY * TILE_SIZE + HITBOX_OFFSET_Y,
                HITBOX_WIDTH, HITBOX_HEIGHT
        );

        for (Enemy enemy : enemies) {
            if (!enemy.isDead() && futureHitbox.intersects(enemy.getHitbox())) {
                System.out.println("COLIZIUNE DETECTATA");

                return;
            }
        }

        if (newY >= 0 && newY < layer1.length && newX >= 0 && newX < layer1[0].length) {
            int tile1 = layer1[newY][newX];
            int tile2 = layer2[newY][newX];

            boolean baseWalkable = walkableTiles.contains(tile1);

            if (baseWalkable) {
                x = newX;
                y = newY;
                updateHitbox(TILE_SIZE);
            }
        }
    }
}
