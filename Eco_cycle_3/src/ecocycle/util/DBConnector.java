// In file: src/ecocycle/util/DBConnector.java
package ecocycle.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnector {

    // --- !! YOU MUST EDIT THESE 3 LINES !! ---
    private static final String URL = "jdbc:mysql://localhost:3306/ecocycle_db?useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "Herondale@32"; // <-- !! EDIT THIS !!

    // Private constructor to prevent instantiation
    private DBConnector() {}

    /**
     * Gets a NEW, fresh connection to the database every time.
     * This prevents one method from closing another's connection.
     * @return A new database connection.
     * @throws SQLException if the connection fails.
     */
    public static Connection getConnection() throws SQLException {
        try {
            // This line is not strictly needed for MySQL 8+ but is good practice
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // This line is the fix: It *always* returns a new connection.
            return DriverManager.getConnection(URL, USER, PASSWORD);
            
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
            // Re-throw as an SQLException so the calling method knows it failed
            throw new SQLException("JDBC Driver not found", e);
        }
    }
}