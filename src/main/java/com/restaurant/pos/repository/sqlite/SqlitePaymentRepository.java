package com.restaurant.pos.repository.sqlite;

import com.restaurant.pos.database.ConnectionManager;
import com.restaurant.pos.model.Money;
import com.restaurant.pos.model.Payment;
import com.restaurant.pos.model.PaymentMethod;
import com.restaurant.pos.repository.PaymentRepository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;

public final class SqlitePaymentRepository implements PaymentRepository {

    private final ConnectionManager connectionManager;

    public SqlitePaymentRepository(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Optional<Payment> findByOrderId(long orderId) {
        return connectionManager.execute(connection -> {
            try (PreparedStatement stmt = connection.prepareStatement(
                    "SELECT * FROM payments WHERE order_id = ?")) {
                stmt.setLong(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
                }
            }
        });
    }

    private static Payment mapRow(ResultSet rs) throws SQLException {
        return Payment.builder()
                .id(rs.getLong("id"))
                .orderId(rs.getLong("order_id"))
                .method(PaymentMethod.valueOf(rs.getString("method")))
                .amountTendered(Money.ofMinorUnits(rs.getLong("amount_tendered_minor")))
                .changeGiven(Money.ofMinorUnits(rs.getLong("change_given_minor")))
                .paidAt(Instant.ofEpochMilli(rs.getLong("paid_at_epoch_ms")))
                .build();
    }
}
