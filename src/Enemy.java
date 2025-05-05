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
    private final int spriteSize = 64; // dimensiunea sprite-ului desenat pe hartă

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
        loadIdleSprites();
    }

    private void loadIdleSprites() {
        try {
            BufferedImage sheet = ImageIO.read(new File("assets/sprites/enemy/Idle.png"));
            System.out.println("Enemy sprite width: " + sheet.getWidth() + ", height: " + sheet.getHeight());

            idleFrames = new Image[frameCount];
            for (int i = 0; i < frameCount; i++) {
                BufferedImage frame = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
                idleFrames[i] = frame.getScaledInstance(spriteSize, spriteSize, Image.SCALE_SMOOTH);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g, int tileSize) {
        int drawX = x * tileSize + (tileSize - spriteSize) / 2;
        int drawY = y * tileSize + (tileSize - spriteSize); // pentru aliniere verticală mai naturală

        if (idleFrames[currentFrame] != null) {
            g.drawImage(idleFrames[currentFrame], drawX, drawY, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(drawX, drawY, tileSize, tileSize);
        }
    }

    public void updateAnimation() {
        currentFrame = (currentFrame + 1) % frameCount;
    }

    public Rectangle getBounds(int tileSize) {
        return new Rectangle(x * tileSize, y * tileSize, tileSize, tileSize);
    }
}
