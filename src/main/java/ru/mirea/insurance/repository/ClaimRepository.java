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
import ru.mirea.insurance.model.Claim;
import ru.mirea.insurance.model.ClaimStatus;
import ru.mirea.insurance.util.ConnectionSource;

/** Доступ к таблице claims. Удаление полиса уносит его убытки — ON DELETE CASCADE в схеме. */
@Repository
public class ClaimRepository implements CrudRepository<Claim> {
    private static final String COLUMNS =
            "id, policy_id, event_date, submitted_at, description, claimed_amount, payout, status";

    private final ConnectionSource connectionSource;

    public ClaimRepository(ConnectionSource connectionSource) {
        this.connectionSource = connectionSource;
    }

    @Override
    public int save(Claim claim) {
        String sql = "INSERT INTO claims (policy_id, event_date, submitted_at, description, "
                + "claimed_amount, payout, status) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, claim.getPolicyId());
            statement.setDate(2, Date.valueOf(claim.getEventDate()));
            statement.setDate(3, Date.valueOf(claim.getSubmittedAt()));
            statement.setString(4, claim.getDescription());
            statement.setBigDecimal(5, claim.getClaimedAmount());
            statement.setBigDecimal(6, claim.getPayout());
            statement.setString(7, claim.getStatus().name());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
            throw new DataAccessException("База не вернула id нового убытка");
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось сохранить убыток по полису id="
                    + claim.getPolicyId(), e);
        }
    }

    @Override
    public Optional<Claim> findById(int id) {
        String sql = "SELECT " + COLUMNS + " FROM claims WHERE id = ?";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска убытка id=" + id, e);
        }
    }

    @Override
    public List<Claim> findAll() {
        String sql = "SELECT " + COLUMNS + " FROM claims ORDER BY id";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return mapRows(resultSet);
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось получить список убытков", e);
        }
    }

    @Override
    public boolean update(Claim claim) {
        String sql = "UPDATE claims SET policy_id = ?, event_date = ?, submitted_at = ?, "
                + "description = ?, claimed_amount = ?, payout = ?, status = ? WHERE id = ?";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, claim.getPolicyId());
            statement.setDate(2, Date.valueOf(claim.getEventDate()));
            statement.setDate(3, Date.valueOf(claim.getSubmittedAt()));
            statement.setString(4, claim.getDescription());
            statement.setBigDecimal(5, claim.getClaimedAmount());
            statement.setBigDecimal(6, claim.getPayout());
            statement.setString(7, claim.getStatus().name());
            statement.setInt(8, claim.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось изменить убыток id=" + claim.getId(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM claims WHERE id = ?";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Не удалось удалить убыток id=" + id, e);
        }
    }

    /** Все убытки по одному полису — жизненный цикл статусов виден целиком. */
    public List<Claim> findByPolicyId(int policyId) {
        String sql = "SELECT " + COLUMNS + " FROM claims WHERE policy_id = ? ORDER BY event_date";
        try (Connection connection = connectionSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, policyId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapRows(resultSet);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Ошибка поиска убытков по полису id=" + policyId, e);
        }
    }

    private List<Claim> mapRows(ResultSet resultSet) throws SQLException {
        List<Claim> claims = new ArrayList<>();
        while (resultSet.next()) {
            claims.add(mapRow(resultSet));
        }
        return claims;
    }

    private Claim mapRow(ResultSet resultSet) throws SQLException {
        return new Claim(
                resultSet.getInt("id"),
                resultSet.getInt("policy_id"),
                resultSet.getDate("event_date").toLocalDate(),
                resultSet.getDate("submitted_at").toLocalDate(),
                resultSet.getString("description"),
                resultSet.getBigDecimal("claimed_amount"),
                resultSet.getBigDecimal("payout"),
                ClaimStatus.valueOf(resultSet.getString("status")));
    }
}
