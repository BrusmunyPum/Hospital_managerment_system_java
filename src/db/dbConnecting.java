package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbConnecting {

    public static Connection getConnection() {
        Connection conn = null;
        // 1. Get values using our new standard Java Config class
        String host = config.getDbHost();
        String port = config.getDbPort();
        String dbName = config.getDbName();
        String user = config.getDbUser();
        String pass = config.getDbPassword();

        // 2. Build URL
        String url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;

        try {
            // Ensure the driver is loaded
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(url, user, pass);
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection Failed!");
            e.printStackTrace();
        }
        return conn;
    }
}
