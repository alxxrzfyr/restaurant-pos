package com.restaurant.pos.repository.sqlite;

import com.restaurant.pos.database.ConnectionManager;
import com.restaurant.pos.model.Role;
import com.restaurant.pos.model.User;
import com.restaurant.pos.repository.UserRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class SqliteUserRepository implements UserRepository {

    private final ConnectionManager connectionManager;

    public SqliteUserRepository(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<User> findById(long id) {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM users WHERE id = ?")) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
                }
            }
        });
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM users WHERE username = ?")) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
                }
            }
        });
    }

    @Override
    public List<User> findAll() {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM users ORDER BY username");
                 ResultSet rs = stmt.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(mapRow(rs));
                }
                return users;
            }
        });
    }

    @Override
    public User insert(User user) {
        return connectionManager.execute(connection -> {
            long now = System.currentTimeMillis();
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO users (username, display_name, password_hash, role, active, "
                            + "failed_login_attempts, last_failed_login_at_epoch_ms, locked_until_epoch_ms, "
                            + "photo_path, requires_password_change, created_at_epoch_ms, updated_at_epoch_ms) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.username());
                stmt.setString(2, user.displayName());
                stmt.setString(3, user.passwordHash());
                stmt.setString(4, user.role().name());
                stmt.setInt(5, user.active() ? 1 : 0);
                stmt.setInt(6, user.failedLoginAttempts());
                JdbcUtil.setNullableInstant(stmt, 7, user.lastFailedLoginAt());
                JdbcUtil.setNullableInstant(stmt, 8, user.lockedUntil());
                JdbcUtil.setNullableString(stmt, 9, user.photoPath());
                stmt.setInt(10, user.requiresPasswordChange() ? 1 : 0);
                stmt.setLong(11, now);
                stmt.setLong(12, now);
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    keys.next();
                    return user.toBuilder().id(keys.getLong(1)).build();
                }
            }
        });
    }

    @Override
    public void update(User user) {
        connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE users SET display_name = ?, password_hash = ?, role = ?, active = ?, "
                            + "failed_login_attempts = ?, last_failed_login_at_epoch_ms = ?, "
                            + "locked_until_epoch_ms = ?, photo_path = ?, requires_password_change = ?, updated_at_epoch_ms = ? WHERE id = ?")) {
                stmt.setString(1, user.displayName());
                stmt.setString(2, user.passwordHash());
                stmt.setString(3, user.role().name());
                stmt.setInt(4, user.active() ? 1 : 0);
                stmt.setInt(5, user.failedLoginAttempts());
                JdbcUtil.setNullableInstant(stmt, 6, user.lastFailedLoginAt());
                JdbcUtil.setNullableInstant(stmt, 7, user.lockedUntil());
                JdbcUtil.setNullableString(stmt, 8, user.photoPath());
                stmt.setInt(9, user.requiresPasswordChange() ? 1 : 0);
                stmt.setLong(10, System.currentTimeMillis());
                stmt.setLong(11, user.id());
                return stmt.executeUpdate();
            }
        });
    }

    private static User mapRow(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .username(rs.getString("username"))
                .displayName(rs.getString("display_name"))
                .passwordHash(rs.getString("password_hash"))
                .role(Role.valueOf(rs.getString("role")))
                .active(rs.getInt("active") == 1)
                .failedLoginAttempts(rs.getInt("failed_login_attempts"))
                .lastFailedLoginAt(JdbcUtil.getNullableInstant(rs, "last_failed_login_at_epoch_ms"))
                .lockedUntil(JdbcUtil.getNullableInstant(rs, "locked_until_epoch_ms"))
                .photoPath(rs.getString("photo_path"))
                .requiresPasswordChange(rs.getInt("requires_password_change") == 1)
                .build();
    }
}
