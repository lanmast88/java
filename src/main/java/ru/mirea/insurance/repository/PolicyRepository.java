package ru.mirea.insurance.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import ru.mirea.insurance.exception.DataAccessException;
import ru.mirea.insurance.model.Policy;
import ru.mirea.insurance.model.PolicyStatus;
import ru.mirea.insurance.model.PolicyType;
import ru.mirea.insurance.util.ConnectionSource;

/** Доступ к таблице policies: только SQL и маппинг ResultSet → Policy. */
@Repository
public class PolicyRepository implements CrudRepository<Policy> {
    private static final String COLUMNS =
            "id, number, client_id, type, status, insured_sum, premium, start_date, end_date";

    private final ConnectionSource connectionSource;

    public PolicyRepository(ConnectionSource connectionSource) {
        this.connectionSource = connectionSource;
    }

    @Override
    public int save(Policy policy) {
        String sql = "INSERT INTO policies (number, client_id, type, status, insured_sum, premium, "
                + "start_date, end_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, policy.getNumber());
            statement.setInt(2, policy.getClientId());
            statement.setString(3, policy.getType().name());
            statement.setString(4, policy.getStatus().name());
            statement.setBigDecimal(5, policy.getInsuredSum());
            statement.setBigDecimal(6, policy.getPremium());
            statement.setDate(7, Date.valueOf(policy.getStartDate()));
            statement.setDate(8, Date.valueOf(policy.getEndDate()));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
            throw new DataAccessException("База не вернула id нового полиса");
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось сохранить полис " + policy.getNumber(), e);
        }
    }

    @Override
    public Optional<Policy> findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM policies WHERE id = ?";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска полиса id=" + id, e);
        }
    }

    @Override
    public List<Policy> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM policies ORDER BY id";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapRows(resultSet);
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось получить список полисов", e);
        }
    }

    @Override
    public boolean update(Policy policy) {
        String sql = "UPDATE policies SET number = ?, client_id = ?, type = ?, status = ?, "
                + "insured_sum = ?, premium = ?, start_date = ?, end_date = ? WHERE id = ?";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, policy.getNumber());
            statement.setInt(2, policy.getClientId());
            statement.setString(3, policy.getType().name());
            statement.setString(4, policy.getStatus().name());
            statement.setBigDecimal(5, policy.getInsuredSum());
            statement.setBigDecimal(6, policy.getPremium());
            statement.setDate(7, Date.valueOf(policy.getStartDate()));
            statement.setDate(8, Date.valueOf(policy.getEndDate()));
            statement.setInt(9, policy.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось изменить полис id=" + policy.getId(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM policies WHERE id = ?";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось удалить полис id=" + id, e);
        }
    }

    /** Нужен бизнес-правилам: полисы одного страхователя. */
    public List<Policy> findByClientId(int clientId) {
        String sql = "SELECT " + COLUMNS + " FROM policies WHERE client_id = ? ORDER BY id";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, clientId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapRows(resultSet);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска полисов клиента id=" + clientId, e);
        }
    }

    /** Второй способ поиска: точное совпадение по номеру полиса. */
    public Optional<Policy> findByNumber(String number) {
        String sql = "SELECT " + COLUMNS + " FROM policies WHERE number = ?";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, number);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска полиса по номеру " + number, e);
        }
    }

    private List<Policy> mapRows(ResultSet resultSet) throws SQLException {
        List<Policy> policies = new ArrayList<>();
        while (resultSet.next()) {
            policies.add(mapRow(resultSet));
        }
        return policies;
    }

    /** ResultSet → объект: одно место, где знают про имена колонок. */
    private Policy mapRow(ResultSet resultSet) throws SQLException {
        return new Policy(
                resultSet.getInt("id"),
                resultSet.getString("number"),
                resultSet.getInt("client_id"),
                PolicyType.valueOf(resultSet.getString("type")),
                PolicyStatus.valueOf(resultSet.getString("status")),
                resultSet.getBigDecimal("insured_sum"),
                resultSet.getBigDecimal("premium"),
                resultSet.getDate("start_date").toLocalDate(),
                resultSet.getDate("end_date").toLocalDate());
    }
}
