package com.restaurant.pos.database;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionManager {

    private final DataSource dataSource;

    public ConnectionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(ConnectionCallback<T> callback) {
        try (Connection connection = dataSource.getConnection()) {
            return callback.doWithConnection(connection);
        } catch (SQLException e) {
            throw new PersistenceException("Database operation failed", e);
        }
    }

    public <T> T inTransaction(ConnectionCallback<T> callback) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                T result = callback.doWithConnection(connection);
                connection.commit();
                return result;
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenceException("Transactional database operation failed", e);
        }
    }
}
