import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class AnimatedSprite {
    private BufferedImage[] frames;
    private int currentFrame = 0;
    private long lastFrameTime;
    private int frameDelay;

    public AnimatedSprite(String path, int frameCount, int frameDelay) {
        this.frameDelay = frameDelay;
        try {
            BufferedImage spriteSheet = ImageIO.read(new File(path));
            int frameWidth = spriteSheet.getWidth() / frameCount;
            int frameHeight = spriteSheet.getHeight();
            frames = new BufferedImage[frameCount];
            for (int i = 0; i < frameCount; i++) {
                frames[i] = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
            lastFrameTime = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics g, int x, int y, int width, int height, Component observer) {
        updateFrame();
        if (frames != null && frames[currentFrame] != null) {
            g.drawImage(frames[currentFrame], x, y, width, height, observer);
        }
    }

    public void draw(Graphics g, int x, int y, int width, int height, Component observer, boolean flipHorizontal) {
        updateFrame();
        if (frames != null && frames[currentFrame] != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            if (flipHorizontal) {
                g2.translate(x + width, y);
                g2.scale(-1, 1);
                g2.drawImage(frames[currentFrame], 0, 0, width, height, observer);
            } else {
                g2.drawImage(frames[currentFrame], x, y, width, height, observer);
            }
            g2.dispose();
        }
    }

    private void updateFrame() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime >= frameDelay) {
            currentFrame = (currentFrame + 1) % frames.length;
            lastFrameTime = currentTime;
        }
    }
}
