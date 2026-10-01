package ru.mirea.insurance.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import ru.mirea.insurance.exception.DataAccessException;
import ru.mirea.insurance.exception.EntityNotFoundException;
import ru.mirea.insurance.util.ConnectionSource;

/** Пункт задания «Вывести таблицы базы данных»: системный каталог и дамп любой таблицы. */
@Repository
public class SchemaRepository {
    private final ConnectionSource connectionSource;

    public SchemaRepository(ConnectionSource connectionSource) {
        this.connectionSource = connectionSource;
    }

    /** Список таблиц схемы public из системного каталога information_schema. */
    public List<String> listTables() {
        String sql = "SELECT table_name FROM information_schema.tables "
                + "WHERE table_schema = 'public' AND table_type = 'BASE TABLE' ORDER BY table_name";
        List<String> tables = new ArrayList<>();
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tables.add(resultSet.getString("table_name"));
            }
            return tables;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось получить список таблиц базы данных", e);
        }
    }

    /**
     * Содержимое выбранной таблицы. Имя таблицы нельзя передать параметром
     * PreparedStatement, поэтому оно сверяется со списком из каталога — произвольная
     * строка в запрос не попадёт.
     */
    public TableSnapshot dumpTable(String table) {
        if (!listTables().contains(table)) {
            throw new EntityNotFoundException("Таблицы «" + table + "» нет в базе данных");
        }
        String sql = "SELECT * FROM \"" + table + "\"";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int count = metaData.getColumnCount();
            List<String> headers = new ArrayList<>(count);
            List<Boolean> numeric = new ArrayList<>(count);
            for (int i = 1; i <= count; i++) {
                headers.add(metaData.getColumnLabel(i));
                numeric.add(isNumeric(metaData.getColumnType(i)));
            }
            List<List<String>> rows = new ArrayList<>();
            while (resultSet.next()) {
                List<String> cells = new ArrayList<>(count);
                for (int i = 1; i <= count; i++) {
                    String value = resultSet.getString(i);
                    cells.add(value == null ? "NULL" : value);
                }
                rows.add(cells);
            }
            return new TableSnapshot(table, headers, numeric, rows);
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось прочитать таблицу " + table, e);
        }
    }

    private static boolean isNumeric(int sqlType) {
        return sqlType == Types.INTEGER || sqlType == Types.BIGINT || sqlType == Types.SMALLINT
                || sqlType == Types.NUMERIC || sqlType == Types.DECIMAL
                || sqlType == Types.DOUBLE || sqlType == Types.REAL || sqlType == Types.FLOAT;
    }
}
