import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/ev_charging_db?useSSL=false&allowPublicKeyRetrieval=true",
                    "root",
                    "root123" // Adjust password if needed
            );
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found in classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Database connection error: " + e.getMessage(), e);
        }
        return con;
    }
}