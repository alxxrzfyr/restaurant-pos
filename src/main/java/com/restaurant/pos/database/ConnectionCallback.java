package com.restaurant.pos.database;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface ConnectionCallback<T> {
    T doWithConnection(Connection connection) throws SQLException;
}
