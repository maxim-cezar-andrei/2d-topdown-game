import java.sql.*;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:test.db");
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS test (id INTEGER PRIMARY KEY, name TEXT)");
            stmt.execute("INSERT INTO test (name) VALUES ('Nyx')");
            ResultSet rs = stmt.executeQuery("SELECT * FROM test");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + " | Name: " + rs.getString("name"));
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
