import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataBaseManager {
    private static final String DB_URL = "jdbc:sqlite:shadow_heist.db";
    private Connection conn;

    public DataBaseManager() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            createTablesIfNotExist();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTablesIfNotExist() throws SQLException {
        String createGameState = """
            CREATE TABLE IF NOT EXISTS game_state (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nyx_x INTEGER,
                nyx_y INTEGER
            );
        """;

        String createEnemies = """
            CREATE TABLE IF NOT EXISTS enemies (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                game_id INTEGER,
                x INTEGER,
                y INTEGER,
                is_dead BOOLEAN,
                FOREIGN KEY (game_id) REFERENCES game_state(id)
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createGameState);
            stmt.execute(createEnemies);
        }
    }

    public void saveGame(Nyx nyx, List<Enemy> enemies) {
        try {
            String insertGame = "INSERT INTO game_state(nyx_x, nyx_y) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertGame, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, nyx.getXTile());
                ps.setInt(2, nyx.getYTile());
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                int gameId = rs.next() ? rs.getInt(1) : 0;

                String insertEnemy = "INSERT INTO enemies(game_id, x, y, is_dead) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pe = conn.prepareStatement(insertEnemy)) {
                    for (Enemy e : enemies) {
                        pe.setInt(1, gameId);
                        pe.setInt(2, e.getX());
                        pe.setInt(3, e.getY());
                        pe.setBoolean(4, e.isDead());
                        pe.addBatch();
                    }
                    pe.executeBatch();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public GameState loadLastGame() {
        try {
            String selectGame = "SELECT * FROM game_state ORDER BY id DESC LIMIT 1";
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(selectGame)) {
                if (rs.next()) {
                    int gameId = rs.getInt("id");
                    int nyxX = rs.getInt("nyx_x");
                    int nyxY = rs.getInt("nyx_y");

                    List<Enemy> enemies = new ArrayList<>();
                    String selectEnemies = "SELECT * FROM enemies WHERE game_id = ?";
                    try (PreparedStatement pe = conn.prepareStatement(selectEnemies)) {
                        pe.setInt(1, gameId);
                        try (ResultSet re = pe.executeQuery()) {
                            while (re.next()) {
                                int x = re.getInt("x");
                                int y = re.getInt("y");
                                boolean isDead = re.getBoolean("is_dead");
                                Enemy enemy = new Enemy(x, y);
                                if (isDead) enemy.die();
                                enemies.add(enemy);
                            }
                        }
                    }

                    Nyx nyx = new Nyx(nyxX, nyxY, enemies);  // Constructorul tău deja acceptă aceste date
                    return new GameState(nyx, enemies);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
