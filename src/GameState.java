import java.util.List;

public class GameState {
    private Nyx nyx;
    private List<Enemy> enemies;

    public GameState(Nyx nyx, List<Enemy> enemies) {
        this.nyx = nyx;
        this.enemies = enemies;
    }

    public Nyx getNyx() { return nyx; }
    public List<Enemy> getEnemies() { return enemies; }
}
