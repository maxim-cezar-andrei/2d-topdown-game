package entities;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class HealthItem {
    private int x, y;
    private Rectangle hitbox;
    private BufferedImage sprite;
    private boolean collected = false;
    private static final int TILE_SIZE = 32;

    public HealthItem(int x, int y) {
        this.x = x;
        this.y = y;
        hitbox = new Rectangle(x*TILE_SIZE, y*TILE_SIZE, TILE_SIZE, TILE_SIZE);
        try {
            sprite = ImageIO.read(getClass().getResource("/sprites/Collectibles/health_orb.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void draw(Graphics2D g2d) {
        if (!collected && sprite != null) {
            g2d.drawImage(sprite, x*TILE_SIZE, y*TILE_SIZE, TILE_SIZE, TILE_SIZE, null);
        }
    }

    public boolean checkCollision(Rectangle nyxHitbox) {
        return hitbox.intersects(nyxHitbox);
    }


    public Rectangle getHitbox() {
        return hitbox;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }
}
