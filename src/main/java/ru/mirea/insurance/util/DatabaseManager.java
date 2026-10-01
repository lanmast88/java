package ru.mirea.insurance.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {
    private static final String URL = envOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/insurance");
    private static final String USER = envOrDefault("DB_USER", "postgres");
    private static final String PASSWORD = envOrDefault("DB_PASSWORD", "postgres");

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String envOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
