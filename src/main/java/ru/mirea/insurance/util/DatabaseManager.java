package ru.mirea.insurance.util;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseManager {
    private static final Properties ENV = loadEnvFile(Path.of(".env"));

    private static final String URL = require("DB_URL");
    private static final String USER = require("DB_USER");
    private static final String PASSWORD = require("DB_PASSWORD");

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static Properties loadEnvFile(Path path) {
        Properties properties = new Properties();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                properties.load(reader);
            } catch (IOException e) {
                throw new IllegalStateException("Не удалось прочитать " + path, e);
            }
        }
        return properties;
    }

    // Переменная окружения ОС важнее .env — так можно переопределить значение без правки файла
    private static String require(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = ENV.getProperty(key);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Не задан параметр " + key + " в .env или переменных окружения");
        }
        return value;
    }
}
