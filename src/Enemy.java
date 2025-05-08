import java.awt.*;
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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g, int tileSize) {
        int drawX = x * tileSize + (tileSize - spriteSize) / 2;
        int drawY = y * tileSize + (tileSize - spriteSize);

        if (isDead) {
            g.drawImage(deathFrames[Math.min(deathCurrentFrame, deathFrameCount - 1)], drawX, drawY, null);
        } else {
            g.drawImage(idleFrames[currentFrame], drawX, drawY, null);
        }

        g.setColor(Color.RED);
        g.draw(hitbox);
        g.setColor(new Color(255, 0, 0, 50));  // roșu transparent
        g.fill(hitbox);
    }

    public void updateAnimation() {
        if (isDead) {
            if (deathCurrentFrame < deathFrameCount - 1) {
                deathCurrentFrame++;
            }
        } else {
            currentFrame = (currentFrame + 1) % frameCount;
        }
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

    public int getY()
    {
        return y;
    }
}  // end Enemy
