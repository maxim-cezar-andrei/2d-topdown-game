import java.util.List;

public class GameState {
    private Nyx nyx;
    private List<Enemy> enemies;
    private int mapId;

    public GameState(Nyx nyx, List<Enemy> enemies, int mapId) {
        this.nyx = nyx;
        this.enemies = enemies;
        this.mapId = mapId;
    }

    public Nyx getNyx() {
        return nyx;
    }
    public List<Enemy> getEnemies() {
        return enemies;
    }
    public int getMapId() {
        return mapId;
    }
}
