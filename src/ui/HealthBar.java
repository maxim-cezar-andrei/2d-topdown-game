package ui;

import entities.Nyx;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class HealthBar {
    private BufferedImage spriteSheet;
    private final int SEGMENT_COUNT = 4;
    private int frameHeight;
    private int frameWidth;

    public HealthBar() {
        try {
            spriteSheet = ImageIO.read(Objects.requireNonNull(
                    getClass().getResource("/sprites/Hud/health_bar.png")
            ));

            frameWidth = spriteSheet.getWidth();
            frameHeight = spriteSheet.getHeight() / SEGMENT_COUNT;
        } catch (IOException e) {
            System.err.println("Eroare la încărcarea sprite-ului health_bar.png");
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2d, Nyx nyx, int x, int y) {
        int hp = Math.max(0, Math.min(3, nyx.getMaxHealth() - nyx.getHealth())); // între 0 și 3

        BufferedImage frame = spriteSheet.getSubimage(
                0,
                hp * frameHeight,
                frameWidth,
                frameHeight
        );

        int scaledWidth = 90;
        int scaledHeight = 20;

        g2d.drawImage(frame, x, y, scaledWidth, scaledHeight, null);
    }

}
