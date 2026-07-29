package com.restaurant.pos.repository.sqlite;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;

final class JdbcUtil {

    private JdbcUtil() {
    }

    static void setNullableLong(PreparedStatement stmt, int index, Long value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.INTEGER);
        } else {
            stmt.setLong(index, value);
        }
    }

    static Long getNullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }

    static Instant getNullableInstant(ResultSet rs, String column) throws SQLException {
        Long epochMs = getNullableLong(rs, column);
        return epochMs == null ? null : Instant.ofEpochMilli(epochMs);
    }

    static void setNullableInstant(PreparedStatement stmt, int index, Instant value) throws SQLException {
        setNullableLong(stmt, index, value == null ? null : value.toEpochMilli());
    }

    static void setNullableString(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.VARCHAR);
        } else {
            stmt.setString(index, value);
        }
    }
}
