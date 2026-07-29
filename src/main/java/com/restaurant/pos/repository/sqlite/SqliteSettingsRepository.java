package com.restaurant.pos.repository.sqlite;

import com.restaurant.pos.database.ConnectionManager;
import com.restaurant.pos.repository.SettingsRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class SqliteSettingsRepository implements SettingsRepository {

    private final ConnectionManager connectionManager;

    public SqliteSettingsRepository(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<String> get(String key) {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT value FROM settings WHERE key = ?")) {
                stmt.setString(1, key);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? Optional.of(rs.getString("value")) : Optional.empty();
                }
            }
        });
    }

    @Override
    public Map<String, String> getAll() {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement("SELECT key, value FROM settings");
                 ResultSet rs = stmt.executeQuery()) {
                Map<String, String> settings = new LinkedHashMap<>();
                while (rs.next()) {
                    settings.put(rs.getString("key"), rs.getString("value"));
                }
                return settings;
            }
        });
    }

    @Override
    public void set(String key, String value) {
        connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO settings (key, value) VALUES (?, ?) "
                            + "ON CONFLICT(key) DO UPDATE SET value = excluded.value")) {
                stmt.setString(1, key);
                stmt.setString(2, value);
                return stmt.executeUpdate();
            }
        });
    }
}
