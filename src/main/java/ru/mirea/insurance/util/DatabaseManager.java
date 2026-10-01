package ru.mirea.insurance.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

/**
 * Доступ к JDBC-соединениям. DataSource создаёт Spring Boot по настройкам
 * spring.datasource.* из application.yml; за ним стоит пул HikariCP, поэтому
 * getConnection() берёт готовое соединение, а close() возвращает его в пул.
 */
@Component
public class DatabaseManager {
    private final DataSource dataSource;

    public DatabaseManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Соединение для репозиториев. Вызывающий обязан закрыть его —
     * try (Connection connection = databaseManager.getConnection()) { ... }
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /** Строка «БД и версия сервера» для стартовой проверки подключения. */
    public String describeConnection() {
        try (Connection connection = getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            return metaData.getURL() + " (" + metaData.getDatabaseProductName()
                    + " " + metaData.getDatabaseProductVersion() + ")";
        } catch (SQLException e) {
            return "нет подключения — " + e.getMessage();
        }
    }
}
