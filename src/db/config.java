package db;

import java.util.Properties;
import java.io.FileInputStream;
import java.io.IOException;

public class config {
    private static Properties properties = new Properties();

    // Static block to load the file once when the app starts
    static {
        try (FileInputStream input = new FileInputStream(".env")) {
            properties.load(input);
        } catch (IOException e) {
            System.out.println("Error: Could not find or load .env file.");
            e.printStackTrace();
        }
    }

    public static String getDbUser() {
        return properties.getProperty("DB_USER");
    }

    public static String getDbPassword() {
        return properties.getProperty("DB_PASSWORD");
    }

    public static String getDbName() {
        return properties.getProperty("DB_NAME");
    }

    public static String getDbPort() {
        return properties.getProperty("PORT");
    }

    public static String getDbHost() {
        return properties.getProperty("HOST");
    }
}
