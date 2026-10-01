package ru.mirea.insurance.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;

import ru.mirea.insurance.exception.DataAccessException;
import ru.mirea.insurance.model.Client;
import ru.mirea.insurance.util.DatabaseManager;

/** Доступ к таблице clients. Единственное место, где знают про её SQL и имена колонок. */
@Repository
public class ClientRepository {
    private final DatabaseManager databaseManager;

    public ClientRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public List<Client> findAll() {
        String sql = "SELECT id, last_name, first_name, phone FROM clients ORDER BY id";
        List<Client> clients = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                clients.add(mapRow(resultSet));
            }
            return clients;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось получить список клиентов", e);
        }
    }

    /** ResultSet → объект модели. */
    private Client mapRow(ResultSet resultSet) throws SQLException {
        return new Client(
                resultSet.getInt("id"),
                resultSet.getString("last_name"),
                resultSet.getString("first_name"),
                resultSet.getString("phone"));
    }
}
