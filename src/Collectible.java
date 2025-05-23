import java.awt.*;
import java.awt.image.BufferedImage;

public class Collectible {
    private static final int TILE_SIZE = 32;
    private int x, y;
    private boolean collected;
    private final int points = 10;
    private BufferedImage sprite;

    public Collectible(int x, int y, BufferedImage sprite) {
        this.x = x;
        this.y = y;
        this.sprite = sprite;
        this.collected = false;
    }

    public void draw(Graphics g) {
        if (!collected) {
            g.drawImage(sprite, x * TILE_SIZE, y * TILE_SIZE, null);
        }
    }

    public boolean checkCollision(Rectangle nyxHitbox) {
        Rectangle hitbox = new Rectangle(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        if (!collected && nyxHitbox.intersects(hitbox)) {
            collected = true;
            return true;
        }
        return false;
    }

    public int getPoints() {
        return points;
    }

    public boolean isCollected() {
        return collected;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
