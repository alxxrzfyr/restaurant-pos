package com.restaurant.pos.repository.sqlite;

import com.restaurant.pos.database.ConnectionManager;
import com.restaurant.pos.model.AuditEvent;
import com.restaurant.pos.model.AuditEventType;
import com.restaurant.pos.repository.AuditEventRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class SqliteAuditEventRepository implements AuditEventRepository {

    private final ConnectionManager connectionManager;

    public SqliteAuditEventRepository(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public AuditEvent insert(AuditEvent event) {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "INSERT INTO audit_events (occurred_at_epoch_ms, user_id, username, event_type, details, hash, prev_hash) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, event.occurredAt().toEpochMilli());
                JdbcUtil.setNullableLong(stmt, 2, event.userId());
                JdbcUtil.setNullableString(stmt, 3, event.username());
                stmt.setString(4, event.eventType().name());
                JdbcUtil.setNullableString(stmt, 5, event.details());
                JdbcUtil.setNullableString(stmt, 6, event.hash());
                JdbcUtil.setNullableString(stmt, 7, event.prevHash());
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    keys.next();
                    return AuditEvent.builder()
                            .id(keys.getLong(1))
                            .occurredAt(event.occurredAt())
                            .userId(event.userId())
                            .username(event.username())
                            .eventType(event.eventType())
                            .details(event.details())
                            .hash(event.hash())
                            .prevHash(event.prevHash())
                            .build();
                }
            }
        });
    }

    @Override
    public List<AuditEvent> findByDateRange(Instant from, Instant to) {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM audit_events WHERE occurred_at_epoch_ms >= ? AND occurred_at_epoch_ms < ? "
                            + "ORDER BY occurred_at_epoch_ms DESC")) {
                stmt.setLong(1, from.toEpochMilli());
                stmt.setLong(2, to.toEpochMilli());
                try (ResultSet rs = stmt.executeQuery()) {
                    List<AuditEvent> events = new ArrayList<>();
                    while (rs.next()) {
                        events.add(mapRow(rs));
                    }
                    return events;
                }
            }
        });
    }

    @Override
    public java.util.Optional<AuditEvent> findLatest() {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM audit_events ORDER BY id DESC LIMIT 1")) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return java.util.Optional.of(mapRow(rs));
                    }
                    return java.util.Optional.empty();
                }
            }
        });
    }

    private static AuditEvent mapRow(ResultSet rs) throws SQLException {
        return AuditEvent.builder()
                .id(rs.getLong("id"))
                .occurredAt(Instant.ofEpochMilli(rs.getLong("occurred_at_epoch_ms")))
                .userId(JdbcUtil.getNullableLong(rs, "user_id"))
                .username(rs.getString("username"))
                .eventType(AuditEventType.valueOf(rs.getString("event_type")))
                .details(rs.getString("details"))
                .hash(rs.getString("hash"))
                .prevHash(rs.getString("prev_hash"))
                .build();
    }
}
