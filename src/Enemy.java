import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;


public class Enemy {
    private int x, y;
    private Image[] idleFrames;
    private int currentFrame = 0;
    private final int frameCount = 9;
    private final int frameWidth = 128;
    private final int frameHeight = 128;
    private final int spriteSize = 64;

    private Rectangle hitbox;
    private final int HITBOX_WIDTH = 28;
    private final int HITBOX_HEIGHT = 28;
    private final int HITBOX_OFFSET_X = 2;
    private final int HITBOX_OFFSET_Y = 2;

    private boolean isDead = false;
    private Image[] deathFrames;
    private int deathFrameCount = 5;
    private int deathCurrentFrame = 0;

    private boolean isAttacking = false;
    private Image[] attackFrames;
    private final int attackFrameCount = 5;
    private int attackCurrentFrame = 0;
    private int attackCooldown = 0;
    private final int ATTACK_COOLDOWN_MAX = 30;


    private boolean facingRight = true;

    private boolean nyxInRange = false;
    private int inRangeCounter = 0;
    private final int ATTACK_DELAY_BEFORE_FIRST_HIT = 30;


    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
        loadIdleSprites();

        int px = x * 32 + HITBOX_OFFSET_X;
        int py = y * 32 + HITBOX_OFFSET_Y;
        hitbox = new Rectangle(px, py, HITBOX_WIDTH, HITBOX_HEIGHT);
    }

    private void loadIdleSprites() {
        try {
            BufferedImage sheet = ImageIO.read(new File("assets/sprites/Enemy/Idle.png"));
            BufferedImage deathSheet = ImageIO.read(new File("assets/sprites/Enemy/Dead.png"));
            BufferedImage attackSheet = ImageIO.read(new File("assets/sprites/Enemy/Attack_2.png"));

            idleFrames = new Image[frameCount];
            for (int i = 0; i < frameCount; i++) {
                BufferedImage frame = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
                idleFrames[i] = frame.getScaledInstance(spriteSize, spriteSize, Image.SCALE_SMOOTH);
            }

            deathFrames = new Image[deathFrameCount];
            for (int i = 0; i < deathFrameCount; i++) {
                BufferedImage frame = deathSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
                deathFrames[i] = frame.getScaledInstance(spriteSize, spriteSize, Image.SCALE_SMOOTH);
            }

            attackFrames = new Image[attackFrameCount];
            for (int i = 0; i < attackFrameCount; i++) {
                BufferedImage frame = attackSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
                attackFrames[i] = frame.getScaledInstance(spriteSize, spriteSize, Image.SCALE_SMOOTH);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g, int tileSize) {
        int drawX = x * tileSize + (tileSize - spriteSize) / 2;
        int drawY = y * tileSize + (tileSize - spriteSize);

        Image spriteToDraw;

        if (isDead) {
            spriteToDraw = deathFrames[Math.min(deathCurrentFrame, deathFrameCount - 1)];
        } else if (isAttacking) {
            spriteToDraw = attackFrames[Math.min(attackCurrentFrame, attackFrameCount - 1)];
        } else {
            spriteToDraw = idleFrames[currentFrame];
        }

        if (!facingRight) {
            spriteToDraw = flipImageHorizontally(spriteToDraw);
        }

        g.drawImage(spriteToDraw, drawX, drawY, null);

        // Desenează hitbox-ul pentru debug (opțional)
        g.setColor(Color.RED);
        g.draw(hitbox);
        g.setColor(new Color(255, 0, 0, 50));
        g.fill(hitbox);
    }

    public void updateAnimation() {
        if (isDead) {
            if (deathCurrentFrame < deathFrameCount - 1) {
                deathCurrentFrame++;
            }
            return;
        }

        if (isAttacking) {
            if (attackCurrentFrame < attackFrameCount - 1) {
                attackCurrentFrame++;
            } else {
                isAttacking = false;
                attackCurrentFrame = 0;
                resetAttackCooldown();
            }
        } else {
            currentFrame = (currentFrame + 1) % frameCount;
        }
        if (attackCooldown > 0) attackCooldown--;
    }

    public void Proximity(int nyxX, int nyxY) {
        if (isNear(nyxX, nyxY)) {
            if (!nyxInRange) {
                nyxInRange = true;
                inRangeCounter = 0;
            } else {
                inRangeCounter++;
            }
        } else {
            nyxInRange = false;
            inRangeCounter = 0;
        }
    }

    public void startAttack() {
        if (!isAttacking){
                isAttacking = true;
                attackCurrentFrame = 0;
        }
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

    public void updateDirection(int nyxX) {
        if (nyxX < x) {
            facingRight = false; // Nyx e în stânga
        } else {
            facingRight = true;  // Nyx e în dreapta sau pe aceeași poziție
        }
    }

    public boolean isNear(int px, int py) {
        return Math.abs(px - x) + Math.abs(py - y) == 1;
    }

    public boolean canAttack() {
        return nyxInRange && inRangeCounter >= ATTACK_DELAY_BEFORE_FIRST_HIT && attackCooldown <= 0;
    }

    public void resetAttackCooldown() {
        attackCooldown = ATTACK_COOLDOWN_MAX;
    }

    public void updateHitbox(int TILE_SIZE) {
        hitbox.setLocation(x * 32 + HITBOX_OFFSET_X, y * 32 + HITBOX_OFFSET_Y);
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public boolean isDead() {
        return isDead;
    }

    public void die() {
        isDead = true;
        deathCurrentFrame = 0;
    }

    public int getX()
    {
        return x;
    }
    public int getY()
    {
        return y;
    }

}  // end Enemy
