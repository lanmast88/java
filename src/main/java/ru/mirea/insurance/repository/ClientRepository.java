package ru.mirea.insurance.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import ru.mirea.insurance.exception.DataAccessException;
import ru.mirea.insurance.model.Client;
import ru.mirea.insurance.util.ConnectionSource;

/** Доступ к таблице clients. Единственное место, где знают про её SQL и имена колонок. */
@Repository
public class ClientRepository implements CrudRepository<Client> {
    private final ConnectionSource connectionSource;

    public ClientRepository(ConnectionSource connectionSource) {
        this.connectionSource = connectionSource;
    }

    @Override
    public int save(Client client) {
        String sql = "INSERT INTO clients (last_name, first_name, phone) VALUES (?, ?, ?) RETURNING id";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, client.getLastName());
            statement.setString(2, client.getFirstName());
            statement.setString(3, client.getPhone());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
            throw new DataAccessException("База не вернула id нового клиента");
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось сохранить клиента", e);
        }
    }

    @Override
    public Optional<Client> findById(int id) {
        String sql = "SELECT id, last_name, first_name, phone FROM clients WHERE id = ?";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска клиента id=" + id, e);
        }
    }

    @Override
    public List<Client> findAll() {
        String sql = "SELECT id, last_name, first_name, phone FROM clients ORDER BY last_name, first_name";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapRows(resultSet);
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось получить список клиентов", e);
        }
    }

    @Override
    public boolean update(Client client) {
        String sql = "UPDATE clients SET last_name = ?, first_name = ?, phone = ? WHERE id = ?";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, client.getLastName());
            statement.setString(2, client.getFirstName());
            statement.setString(3, client.getPhone());
            statement.setInt(4, client.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось изменить клиента id=" + client.getId(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM clients WHERE id = ?";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось удалить клиента id=" + id, e);
        }
    }

    /**
     * Поиск по ФИО: подстрока передаётся параметром, проценты навешиваются
     * на значение, а не на текст запроса — склейки строк в SQL нет.
     */
    public List<Client> searchByName(String part) {
        String sql = "SELECT id, last_name, first_name, phone FROM clients "
                + "WHERE last_name ILIKE ? OR first_name ILIKE ? ORDER BY last_name, first_name";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            String pattern = "%" + part + "%";
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapRows(resultSet);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска клиентов по ФИО «" + part + "»", e);
        }
    }

    private List<Client> mapRows(ResultSet resultSet) throws SQLException {
        List<Client> clients = new ArrayList<>();
        while (resultSet.next()) {
            clients.add(mapRow(resultSet));
        }
        return clients;
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
