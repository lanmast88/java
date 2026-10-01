package ru.mirea.insurance.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.stereotype.Component;

@Component
public class DatabaseManager implements ConnectionSource {
    private final DataSource dataSource;

    public DatabaseManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /** Вызывающий обязан закрыть соединение: try-with-resources вернёт его в пул. */
    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

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
